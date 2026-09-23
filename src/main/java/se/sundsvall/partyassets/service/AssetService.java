package se.sundsvall.partyassets.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.support.Relation;
import se.sundsvall.partyassets.api.model.*;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.AssetRevisionRepository;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.AssetRevisionEntity;
import se.sundsvall.partyassets.integration.party.PartyTypeProvider;
import se.sundsvall.partyassets.integration.relation.RelationClient;
import se.sundsvall.partyassets.service.mapper.AssetMapper;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static se.sundsvall.partyassets.api.model.Status.ACTIVE;
import static se.sundsvall.partyassets.api.model.Status.DRAFT;
import static se.sundsvall.partyassets.api.model.Status.REPLACED;
import static se.sundsvall.partyassets.integration.db.specification.AssetSpecification.createAssetSpecification;
import static se.sundsvall.partyassets.integration.db.specification.AssetSpecification.createAssetSpecificationExcludingDraftAsssets;
import static se.sundsvall.partyassets.service.AssetRevisions.snapshot;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.toCopyEntity;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.toEntity;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.updateEntity;
import static se.sundsvall.partyassets.service.mapper.AssetRevisionMapper.currentActor;
import static se.sundsvall.partyassets.service.mapper.RelationMapper.toRelation;

@Service
@Transactional
public class AssetService {

	private static final String ASSET_NOT_FOUND_TITLE = "Asset not found";
	private static final String ASSET_NOT_FOUND_DETAIL = "Asset with id %s not found for municipalityId %s";
	private static final String INVALID_SOURCE_REFERENCE_TITLE = "Invalid source reference";
	private static final String INVALID_SOURCE_REFERENCE_DETAIL = "Provided source reference '%s' is invalid. Expected format: '{relationType}|{sourceResourceId};{sourceType};{sourceService};{sourceNamespace}|'";

	private final AssetRepository repository;
	private final AssetRevisionRepository assetRevisionRepository;
	private final PartyTypeProvider partyTypeProvider;
	private final RelationClient relationClient;

	public AssetService(final AssetRepository repository, final AssetRevisionRepository assetRevisionRepository, final PartyTypeProvider partyTypeProvider, final RelationClient relationClient) {
		this.repository = repository;
		this.assetRevisionRepository = assetRevisionRepository;
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

	public VersionedAsset getAsset(final String municipalityId, final String id) {
		final var entity = getAssetEntity(municipalityId, id);
		return new VersionedAsset(AssetMapper.toAsset(entity), entity.getVersion());
	}

	public String createAsset(final String municipalityId, final AssetCreateRequest request, final String sourceReference) {
		if (isNotBlank(request.getAssetId()) && repository.existsByAssetIdAndMunicipalityId(request.getAssetId(), municipalityId)) {
			throw Problem.builder()
				.withStatus(CONFLICT)
				.withTitle("Asset already exists")
				.withDetail("Asset with assetId %s already exists".formatted(request.getAssetId()))
				.build();
		}

		final var createdAssetId = repository.save(toEntity(request, partyTypeProvider.calculatePartyType(municipalityId, request.getPartyId()), municipalityId)
			.withActor(currentActor())).getId();

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
		return repository.save(toCopyEntity(original).withActor(currentActor())).getId();
	}

	public void updateAsset(final String municipalityId, final String id, final DraftAssetUpdateRequest request, final String ifMatch) {
		final var entity = getAssetEntity(municipalityId, id);
		validatePrecondition(entity, ifMatch);
		if (entity.getStatus() != DRAFT) {
			throw Problem.builder()
				.withStatus(BAD_REQUEST)
				.withTitle("Invalid asset status")
				.withDetail("Only DRAFT assets can be updated via this endpoint")
				.build();
		}
		if (request.getStatus() == ACTIVE) {
			validateValidTo(entity);
			markOriginalAsReplaced(municipalityId, entity.getReplacesId());
		}
		final var revision = snapshot(entity);
		save(updateEntity(entity, request), revision, id);
	}

	public void updateAsset(final String municipalityId, final String id, final AssetUpdateRequest request, final String ifMatch) {
		final var entity = getAssetEntity(municipalityId, id);
		validateNotDraft(entity);
		validatePrecondition(entity, ifMatch);
		final var revision = snapshot(entity);
		save(updateEntity(entity, request), revision, id);
	}

	private void validatePrecondition(final AssetEntity entity, final String ifMatch) {
		if (isBlank(ifMatch)) {
			return;
		}
		if (!ifMatch.trim().equals("\"%d\"".formatted(entity.getVersion()))) {
			throw Problem.builder()
				.withStatus(PRECONDITION_FAILED)
				.withTitle("Asset has changed")
				.withDetail("Asset with id %s does not match the supplied If-Match, please reload it and try again".formatted(entity.getId()))
				.build();
		}
	}

	private void save(final AssetEntity entity, final Optional<AssetRevisionEntity> revision, final String id) {
		try {
			repository.saveAndFlush(entity);
		} catch (final OptimisticLockingFailureException e) {
			throw Problem.builder()
				.withStatus(CONFLICT)
				.withTitle("Asset was updated by someone else")
				.withDetail("Asset with id %s was updated by someone else, please reload it and try again".formatted(id))
				.build();
		}
		revision.ifPresent(assetRevisionRepository::save);
	}

	private void validateNotDraft(final AssetEntity entity) {
		if (entity.getStatus() == DRAFT) {
			throw Problem.builder()
				.withStatus(BAD_REQUEST)
				.withTitle("Invalid asset status")
				.withDetail("DRAFT assets must be updated via the asset drafts resource")
				.build();
		}
	}

	private void validateValidTo(final AssetEntity entity) {
		if (entity.getValidTo() != null && !entity.getValidTo().isAfter(LocalDate.now(ZoneId.systemDefault()))) {
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
				final var revision = snapshot(original);
				original.setStatus(REPLACED);
				save(original, revision, original.getId());
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
		final var parsedRelation = Relation.parseRelation(sourceReference);

		if (Objects.isNull(parsedRelation) || Objects.isNull(parsedRelation.getSource()) || isBlank(parsedRelation.getType())) {
			throw Problem.builder()
				.withStatus(BAD_REQUEST)
				.withTitle(INVALID_SOURCE_REFERENCE_TITLE)
				.withDetail(INVALID_SOURCE_REFERENCE_DETAIL.formatted(sourceReference))
				.build();
		}
		relationClient.createRelation(municipalityId, toRelation(parsedRelation.getType(), parsedRelation, assetId));
	}
}
