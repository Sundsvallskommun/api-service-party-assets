package se.sundsvall.partyassets.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;

@Component
public class AssetExpirationJob {

	private static final String NAME = "asset-expiration";

	private static final Logger LOG = LoggerFactory.getLogger(AssetExpirationJob.class);

	private final AssetExpirationWorker assetExpirationWorker;
	private final Dept44HealthUtility dept44HealthUtility;

	public AssetExpirationJob(final AssetExpirationWorker assetExpirationWorker, final Dept44HealthUtility dept44HealthUtility) {
		this.assetExpirationWorker = assetExpirationWorker;
		this.dept44HealthUtility = dept44HealthUtility;
	}

	@Dept44Scheduled(
		name = NAME,
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
			dept44HealthUtility.setHealthIndicatorUnhealthy(NAME, "%d asset(s) could not be expired".formatted(failures));
		}
	}
}
