package se.sundsvall.partyassets.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@Component
public class AssetExpirationJob {

	private static final Logger LOG = LoggerFactory.getLogger(AssetExpirationJob.class);

	private final AssetExpirationWorker assetExpirationWorker;

	public AssetExpirationJob(final AssetExpirationWorker assetExpirationWorker) {
		this.assetExpirationWorker = assetExpirationWorker;
	}

	// The loop is here rather than in the worker so that each expire call crosses the proxy and gets its own
	// transaction. One asset that cannot be expired is logged and skipped instead of rolling back the rest.
	@Dept44Scheduled(
		name = "asset-expiration",
		cron = "${scheduler.asset-expiration.cron:0 0 0 * * *}",
		lockAtMostFor = "${scheduler.asset-expiration.lock-at-most-for:PT1H}")
	public void run() {
		var failures = 0;

		for (final var assetId : assetExpirationWorker.findExpirableAssetIds()) {
			try {
				assetExpirationWorker.expire(assetId);
			} catch (final Exception e) {
				failures++;
				LOG.error("Could not expire asset {}", assetId, e);
			}
		}

		if (failures > 0) {
			LOG.warn("{} asset(s) could not be expired", failures);
		}
	}
}
