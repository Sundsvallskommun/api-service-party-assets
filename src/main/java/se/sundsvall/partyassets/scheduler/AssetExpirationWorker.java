package se.sundsvall.partyassets.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.AssetRevisionRepository;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

import static se.sundsvall.partyassets.service.mapper.AssetRevisionMapper.currentActor;
import static se.sundsvall.partyassets.service.mapper.AssetRevisionMapper.toRevision;

@Component
public class AssetExpirationWorker {

	private static final Logger LOG = LoggerFactory.getLogger(AssetExpirationWorker.class);

	private static final List<Status> EXPIRABLE_STATUSES = List.of(Status.ACTIVE, Status.TEMPORARY);

	private final AssetRepository assetRepository;
	private final AssetRevisionRepository assetRevisionRepository;

	public AssetExpirationWorker(final AssetRepository assetRepository, final AssetRevisionRepository assetRevisionRepository) {
		this.assetRepository = assetRepository;
		this.assetRevisionRepository = assetRevisionRepository;
	}

	// Ids rather than entities: each one is expired in its own transaction, and a detached entity carried across that
	// boundary would have to be merged back.
	@Transactional(readOnly = true)
	public List<String> findExpirableAssetIds() {
		final var ids = assetRepository.findByStatusInAndValidToBefore(EXPIRABLE_STATUSES, LocalDate.now(ZoneId.systemDefault()))
			.stream()
			.map(AssetEntity::getId)
			.toList();

		LOG.info("Found {} asset(s) to expire", ids.size());
		return ids;
	}

	// One transaction per asset, so a single failure costs that asset rather than the whole night. The loop lives in
	// AssetExpirationJob rather than here: a self-invoked call would run inside the caller's transaction and this
	// annotation would be ignored.
	@Transactional
	public void expire(final String assetId) {
		assetRepository.findById(assetId)
			.filter(asset -> EXPIRABLE_STATUSES.contains(asset.getStatus()))
			.ifPresent(asset -> {
				assetRevisionRepository.save(toRevision(asset));
				asset.setActor(currentActor());
				asset.setStatus(Status.EXPIRED);
				assetRepository.save(asset);
				LOG.info("Expired asset {}", asset.getId());
			});
	}
}
