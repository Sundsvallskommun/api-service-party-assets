package se.sundsvall.partyassets.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.partyassets.api.model.AssetRevision;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.AssetRevisionRepository;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.partyassets.service.mapper.AssetRevisionMapper.toAssetRevision;

/**
 * Reads the history of an asset. Kept apart from AssetService because it never mutates, which is also what keeps
 * AssetMutationGuardTest's list of write paths from growing.
 */
@Service
@Transactional(readOnly = true)
public class AssetRevisionService {

	private static final String ASSET_NOT_FOUND_TITLE = "Asset not found";
	private static final String ASSET_NOT_FOUND_DETAIL = "Asset with id %s not found for municipalityId %s";

	private final AssetRepository assetRepository;
	private final AssetRevisionRepository assetRevisionRepository;

	public AssetRevisionService(final AssetRepository assetRepository, final AssetRevisionRepository assetRevisionRepository) {
		this.assetRepository = assetRepository;
		this.assetRevisionRepository = assetRevisionRepository;
	}

	// Newest first: the asset row is the current revision, and the history table is queried descending, so the result is
	// already ordered without a sort.
	public List<AssetRevision> getRevisions(final String municipalityId, final String id) {
		final var asset = getAssetEntity(municipalityId, id);

		return Stream.concat(
			Stream.of(toAssetRevision(asset)),
			assetRevisionRepository.findByAssetIdOrderByRevisionDesc(id).stream().map(entity -> toAssetRevision(entity)))
			.toList();
	}

	public AssetRevision getRevision(final String municipalityId, final String id, final Integer revision) {
		final var asset = getAssetEntity(municipalityId, id);

		if (Objects.equals(asset.getRevision(), revision)) {
			return toAssetRevision(asset);
		}

		return assetRevisionRepository.findByAssetIdAndRevision(id, revision)
			.map(entity -> toAssetRevision(entity))
			.orElseThrow(() -> Problem.builder()
				.withStatus(NOT_FOUND)
				.withTitle("Revision not found")
				.withDetail("Revision %s of asset %s not found for municipalityId %s".formatted(revision, id, municipalityId))
				.build());
	}

	// Tenancy is enforced here and nowhere else: the asset id is a primary key that has already been proven to belong to
	// this municipality, and asset_revision.municipality_id is a snapshot value rather than an access control column.
	private AssetEntity getAssetEntity(final String municipalityId, final String id) {
		return assetRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> Problem.builder()
				.withStatus(NOT_FOUND)
				.withTitle(ASSET_NOT_FOUND_TITLE)
				.withDetail(ASSET_NOT_FOUND_DETAIL.formatted(id, municipalityId))
				.build());
	}
}
