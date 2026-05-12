package se.sundsvall.partyassets.scheduler;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.AssetRepository;

@Component
public class AssetExpirationJob {

	private static final Logger LOG = LoggerFactory.getLogger(AssetExpirationJob.class);

	private final AssetRepository assetRepository;
	private final AssetExpirationWorker assetExpirationWorker;

	public AssetExpirationJob(
		final AssetRepository assetRepository,
		final AssetExpirationWorker assetExpirationWorker) {
		this.assetRepository = assetRepository;
		this.assetExpirationWorker = assetExpirationWorker;
	}

	@Dept44Scheduled(
		name = "asset-expiration",
		cron = "${scheduler.asset-expiration.cron:0 0 0 * * *}",
		lockAtMostFor = "${scheduler.asset-expiration.lock-at-most-for:PT1H}")
	public void run() {
		final var expiredAssets = assetRepository.findByStatusAndValidToBefore(Status.ACTIVE, LocalDate.now());
		LOG.info("Found {} asset(s) to expire", expiredAssets.size());
		expiredAssets.forEach(assetExpirationWorker::expire);
	}
}
