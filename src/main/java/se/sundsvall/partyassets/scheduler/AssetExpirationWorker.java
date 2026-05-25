package se.sundsvall.partyassets.scheduler;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

@Component
public class AssetExpirationWorker {

	private static final Logger LOG = LoggerFactory.getLogger(AssetExpirationWorker.class);

	private final AssetRepository assetRepository;

	public AssetExpirationWorker(final AssetRepository assetRepository) {
		this.assetRepository = assetRepository;
	}

	@Transactional
	public void expireAssets() {
		final var assets = assetRepository.findByStatusInAndValidToBefore(List.of(Status.ACTIVE, Status.TEMPORARY), LocalDate.now());
		LOG.info("Found {} asset(s) to expire", assets.size());
		assets.forEach(this::expire);
	}

	void expire(final AssetEntity asset) {
		asset.setStatus(Status.EXPIRED);
		assetRepository.save(asset);
		LOG.info("Expired asset {}", asset.getId());
	}
}
