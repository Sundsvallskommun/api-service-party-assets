package se.sundsvall.partyassets.scheduler;

import org.springframework.stereotype.Component;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@Component
public class AssetExpirationJob {

	private final AssetExpirationWorker assetExpirationWorker;

	public AssetExpirationJob(final AssetExpirationWorker assetExpirationWorker) {
		this.assetExpirationWorker = assetExpirationWorker;
	}

	@Dept44Scheduled(
		name = "asset-expiration",
		cron = "${scheduler.asset-expiration.cron:0 0 0 * * *}",
		lockAtMostFor = "${scheduler.asset-expiration.lock-at-most-for:PT1H}")
	public void run() {
		assetExpirationWorker.expireAssets();
	}
}
