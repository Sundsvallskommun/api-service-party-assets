package se.sundsvall.partyassets.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.support.Relation;
import se.sundsvall.partyassets.api.model.Asset;
import se.sundsvall.partyassets.api.model.AssetCreateRequest;
import se.sundsvall.partyassets.api.model.AssetPagedResponse;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.api.model.AssetUpdateRequest;
import se.sundsvall.partyassets.api.model.DraftAssetUpdateRequest;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.AssetRevisionRepository;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.AssetRevisionEntity;
import se.sundsvall.partyassets.integration.db.specification.InvalidFilterException;
import se.sundsvall.partyassets.integration.party.PartyTypeProvider;
import se.sundsvall.partyassets.integration.relation.RelationClient;
import se.sundsvall.partyassets.service.mapper.AssetMapper;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static se.sundsvall.partyassets.api.model.Status.ACTIVE;
import static se.sundsvall.partyassets.api.model.Status.DRAFT;
import static se.sundsvall.partyassets.api.model.Status.REPLACED;
import static se.sundsvall.partyassets.api.model.Status.REVOKED;
import static se.sundsvall.partyassets.api.model.Status.TEMPORARY;
import static se.sundsvall.partyassets.integration.db.specification.AssetSpecification.createAssetSpecification;
import static se.sundsvall.partyassets.integration.db.specification.AssetSpecification.excludingDrafts;
import static se.sundsvall.partyassets.integration.db.specification.AssetSpecification.withFilter;
import static se.sundsvall.partyassets.integration.db.specification.AssetSpecification.withMunicipalityId;
import static se.sundsvall.partyassets.service.AssetRevisions.advanceRevision;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.toAssetPagedResponse;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.toCopyEntity;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.toEntity;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.updateEntity;
import static se.sundsvall.partyassets.service.mapper.AssetRevisionMapper.currentActor;
import static se.sundsvall.partyassets.service.mapper.RelationMapper.toRelation;

@Service
@Transactional
public class AssetService {

	private static final String ASSET_NOT_FOUND_TITLE = "Asset not found";
	private static final String INVALID_ASSET_STATUS_TITLE = "Invalid asset status";
	private static final String ASSET_NOT_FOUND_DETAIL = "Asset with id %s not found for municipalityId %s";
	private static final String INVALID_SOURCE_REFERENCE_TITLE = "Invalid source reference";
	private static final Set<Status> CONTENT_MODIFIABLE_STATUSES = EnumSet.of(ACTIVE, TEMPORARY);
	private static final Set<Status> DRAFT_TARGET_STATUSES = EnumSet.of(DRAFT, ACTIVE);
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
		return repository.findAll(createAssetSpecification(municipalityId, request).and(excludingDrafts()))
			.stream()
			.map(AssetMapper::toAsset)
			.toList();
	}

	/**
	 * Returns a page of the assets in the municipality that match the filter, whatever their status. Throws
	 * {@link InvalidFilterException} if the filter cannot be applied to the assets.
	 */
	@Transactional(readOnly = true)
	public AssetPagedResponse searchAssets(final String municipalityId, final Specification<AssetEntity> filter, final Pageable pageable) {
		return toAssetPagedResponse(repository.findAll(withMunicipalityId(municipalityId).and(withFilter(filter)), pageable));
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
				.withTitle(INVALID_ASSET_STATUS_TITLE)
				.withDetail("Only DRAFT assets can be updated via this endpoint")
				.build();
		}
		if (request.getStatus() != null && !DRAFT_TARGET_STATUSES.contains(request.getStatus())) {
			throw Problem.builder()
				.withStatus(BAD_REQUEST)
				.withTitle(INVALID_ASSET_STATUS_TITLE)
				.withDetail("A draft can only be given status %s, not %s".formatted(DRAFT_TARGET_STATUSES, request.getStatus()))
				.build();
		}
		if (request.getStatus() == ACTIVE) {
			validateValidTo(entity);
			replaceOriginal(municipalityId, entity);
		}
		final var revision = advanceRevision(entity);
		save(updateEntity(entity, request), revision, id);
	}

	public void updateAsset(final String municipalityId, final String id, final AssetUpdateRequest request, final String ifMatch) {
		final var entity = getAssetEntity(municipalityId, id);
		validateNotDraft(entity);
		validatePrecondition(entity, ifMatch);
		validateContentModifiable(entity, request);
		final var revision = advanceRevision(entity);
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

	private void save(final AssetEntity entity, final AssetRevisionEntity revision, final String id) {
		try {
			repository.saveAndFlush(entity);
		} catch (final OptimisticLockingFailureException e) {
			throw Problem.builder()
				.withStatus(CONFLICT)
				.withTitle("Asset was updated by someone else")
				.withDetail("Asset with id %s was updated by someone else, please reload it and try again".formatted(id))
				.build();
		}
		ofNullable(revision).ifPresent(assetRevisionRepository::save);
	}

	private void validateNotDraft(final AssetEntity entity) {
		if (entity.getStatus() == DRAFT) {
			throw Problem.builder()
				.withStatus(BAD_REQUEST)
				.withTitle(INVALID_ASSET_STATUS_TITLE)
				.withDetail("DRAFT assets must be updated via the asset drafts resource")
				.build();
		}
	}

	private void validateContentModifiable(final AssetEntity entity, final AssetUpdateRequest request) {
		final var changesContent = Stream.of(request.getValidTo(), request.getIndefinitely(), request.getTitle(), request.getAdditionalParameters(), request.getJsonParameters()).anyMatch(Objects::nonNull);
		if (changesContent && !CONTENT_MODIFIABLE_STATUSES.contains(entity.getStatus())) {
			throw Problem.builder()
				.withStatus(BAD_REQUEST)
				.withTitle("Asset content cannot be modified")
				.withDetail("Content can only be modified on assets with status %s, but asset %s has status %s".formatted(CONTENT_MODIFIABLE_STATUSES, entity.getId(), entity.getStatus()))
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

	/**
	 * Marks the asset that a draft about to be activated replaces as REPLACED when it is active, and leaves it as it is
	 * otherwise. Throws a 400 problem, before anything is changed, when the original has been revoked.
	 */
	private void replaceOriginal(final String municipalityId, final AssetEntity draft) {
		if (draft.getReplacesId() == null) {
			return;
		}
		repository.findByIdAndMunicipalityId(draft.getReplacesId(), municipalityId).ifPresent(original -> {
			if (original.getStatus() == REVOKED) {
				throw Problem.builder()
					.withStatus(BAD_REQUEST)
					.withTitle("Asset cannot be activated")
					.withDetail("Asset %s replaces asset %s, which has status REVOKED".formatted(draft.getId(), original.getId()))
					.build();
			}
			if (original.getStatus() == ACTIVE) {
				final var revision = advanceRevision(original);
				original.changeStatus(REPLACED, null);
				save(original, revision, original.getId());
			}
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
