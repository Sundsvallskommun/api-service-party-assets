package se.sundsvall.partyassets.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.support.Relation;
import se.sundsvall.partyassets.api.model.*;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.party.PartyTypeProvider;
import se.sundsvall.partyassets.integration.relation.RelationClient;
import se.sundsvall.partyassets.service.mapper.AssetMapper;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.partyassets.api.model.Status.ACTIVE;
import static se.sundsvall.partyassets.api.model.Status.DRAFT;
import static se.sundsvall.partyassets.api.model.Status.REPLACED;
import static se.sundsvall.partyassets.integration.db.specification.AssetSpecification.createAssetSpecification;
import static se.sundsvall.partyassets.integration.db.specification.AssetSpecification.createAssetSpecificationExcludingDraftAsssets;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.toCopyEntity;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.toEntity;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.updateEntity;
import static se.sundsvall.partyassets.service.mapper.RelationMapper.toRelation;

@Service
@Transactional
public class AssetService {

	private static final String ASSET_NOT_FOUND_TITLE = "Asset not found";
	private static final String ASSET_NOT_FOUND_DETAIL = "Asset with id %s not found for municipalityId %s";
	private static final String INVALID_SOURCE_REFERENCE_TITLE = "Invalid source reference";
	private static final String INVALID_SOURCE_REFERENCE_DETAIL = "Provided source reference '%s' is invalid. Expected format: '|{sourceResourceId};{sourceType};{sourceService};{sourceNamespace}|'";
	private static final String RELATION_TYPE = "LINK";

	private final AssetRepository repository;
	private final PartyTypeProvider partyTypeProvider;
	private final RelationClient relationClient;

	public AssetService(final AssetRepository repository, final PartyTypeProvider partyTypeProvider, final RelationClient relationClient) {
		this.repository = repository;
		this.partyTypeProvider = partyTypeProvider;
		this.relationClient = relationClient;
	}

	public List<Asset> getAssets(final String municipalityId, final AssetSearchRequest request) {
		return repository.findAll(createAssetSpecification(municipalityId, request).and(createAssetSpecificationExcludingDraftAsssets()))
			.stream()
			.map(AssetMapper::toAsset)
			.toList();
	}

	public List<Asset> getDraftAssets(final String municipalityId, final AssetSearchRequest request) {
		// Explicitly and always use DRAFT status
		return repository.findAll(createAssetSpecification(municipalityId, request.withStatus(DRAFT)))
			.stream()
			.map(AssetMapper::toAsset)
			.toList();
	}

	public Asset getAsset(final String municipalityId, final String id) {
		return repository.findByIdAndMunicipalityId(id, municipalityId)
			.map(AssetMapper::toAsset)
			.orElseThrow(() -> Problem.builder()
				.withStatus(NOT_FOUND)
				.withTitle(ASSET_NOT_FOUND_TITLE)
				.withDetail(ASSET_NOT_FOUND_DETAIL.formatted(id, municipalityId))
				.build());
	}

	public String createAsset(final String municipalityId, final AssetCreateRequest request, final String sourceReference) {
		if (isNotBlank(request.getAssetId()) && repository.existsByAssetIdAndMunicipalityId(request.getAssetId(), municipalityId)) {
			throw Problem.builder()
				.withStatus(CONFLICT)
				.withTitle("Asset already exists")
				.withDetail("Asset with assetId %s already exists".formatted(request.getAssetId()))
				.build();
		}

		final var createdAssetId = repository.save(toEntity(request, partyTypeProvider.calculatePartyType(municipalityId, request.getPartyId()), municipalityId)).getId();

		if (isNotBlank(sourceReference)) {
			createRelation(municipalityId, sourceReference, createdAssetId);
		}
		return createdAssetId;
	}

	public void deleteAsset(final String municipalityId, final String id) {
		if (!repository.existsByIdAndMunicipalityId(id, municipalityId)) {
			throw Problem.builder()
				.withStatus(NOT_FOUND)
				.withTitle(ASSET_NOT_FOUND_TITLE)
				.withDetail(ASSET_NOT_FOUND_DETAIL.formatted(id, municipalityId))
				.build();
		}

		repository.deleteByIdAndMunicipalityId(id, municipalityId);
	}

	public String copyAsset(final String municipalityId, final String id) {
		final var original = getAssetEntity(municipalityId, id);
		if (original.getStatus() != ACTIVE) {
			throw Problem.builder()
				.withStatus(BAD_REQUEST)
				.withTitle("Asset cannot be copied")
				.withDetail("Only ACTIVE assets can be copied, but asset %s has status %s".formatted(id, original.getStatus()))
				.build();
		}

		original.setStatus(REPLACED);
		repository.save(original);
		return repository.save(toCopyEntity(original)).getId();
	}

	public void updateAsset(final String municipalityId, final String id, final DraftAssetUpdateRequest request) {
		final var entity = getAssetEntity(municipalityId, id);
		updateEntity(entity, request);

		if (ACTIVE == request.getStatus()) {
			validateValidTo(entity);
			markOriginalAsReplaced(municipalityId, entity.getReplacesId());
		}

		repository.save(entity);
	}

	public void updateAsset(final String municipalityId, final String id, final AssetUpdateRequest request) {
		repository.save(updateEntity(getAssetEntity(municipalityId, id), request));
	}

	private void validateValidTo(final AssetEntity entity) {
		if (entity.getValidTo() != null && !entity.getValidTo().isAfter(LocalDate.now())) {
			throw Problem.builder()
				.withStatus(BAD_REQUEST)
				.withTitle("Invalid validTo date")
				.withDetail("validTo must be in the future when activating an asset")
				.build();
		}
	}

	private void markOriginalAsReplaced(final String municipalityId, final String replacesId) {
		if (replacesId == null) {
			return;
		}
		repository.findByIdAndMunicipalityId(replacesId, municipalityId)
			.filter(original -> original.getStatus() == ACTIVE)
			.ifPresent(original -> {
				original.setStatus(REPLACED);
				repository.save(original);
			});
	}

	private @NonNull AssetEntity getAssetEntity(String municipalityId, String id) {
		return repository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> Problem.builder()
				.withStatus(NOT_FOUND)
				.withTitle(ASSET_NOT_FOUND_TITLE)
				.withDetail(ASSET_NOT_FOUND_DETAIL.formatted(id, municipalityId))
				.build());
	}

	private void createRelation(String municipalityId, String sourceReference, String assetId) {
		final var relation = toRelation(RELATION_TYPE, Relation.parseRelation(sourceReference), assetId);

		if (Objects.isNull(relation)) {
			throw Problem.builder()
				.withStatus(BAD_REQUEST)
				.withTitle(INVALID_SOURCE_REFERENCE_TITLE)
				.withDetail(INVALID_SOURCE_REFERENCE_DETAIL.formatted(sourceReference))
				.build();
		}
		relationClient.createRelation(municipalityId, relation);
	}
}
