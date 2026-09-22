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
import se.sundsvall.partyassets.integration.db.AssetRepository.AssetIdProjection;
import se.sundsvall.partyassets.integration.db.AssetRevisionRepository;

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

	@Transactional(readOnly = true)
	public List<String> findExpirableAssetIds() {
		final var ids = assetRepository.findByStatusInAndValidToBefore(EXPIRABLE_STATUSES, LocalDate.now(ZoneId.systemDefault()))
			.stream()
			.map(AssetIdProjection::getId)
			.toList();

		LOG.info("Found {} asset(s) to expire", ids.size());
		return ids;
	}

	@Transactional
	public void expire(final String assetId) {
		assetRepository.findById(assetId)
			.filter(asset -> EXPIRABLE_STATUSES.contains(asset.getStatus()))
			.filter(asset -> asset.getValidTo() != null && asset.getValidTo().isBefore(LocalDate.now(ZoneId.systemDefault())))
			.ifPresent(asset -> {
				final var revision = toRevision(asset);
				asset.setActor(currentActor());
				asset.setRevision(asset.getRevision() + 1);
				asset.setStatus(Status.EXPIRED);
				assetRepository.saveAndFlush(asset);
				assetRevisionRepository.save(revision);
				LOG.info("Expired asset {}", asset.getId());
			});
	}
}
