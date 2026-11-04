package se.sundsvall.partyassets.integration.events;

import java.time.OffsetDateTime;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.events.AssetEvent.Asset;
import se.sundsvall.partyassets.integration.events.AssetEvent.EventType;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Optional.ofNullable;
import static se.sundsvall.partyassets.integration.events.AssetEvent.EventType.UPDATED;

public final class AssetEventMapper {

	private AssetEventMapper() {}

	public static AssetEvent toAssetEvent(final AssetEntity entity, final EventType eventType, final OffsetDateTime occurredAt) {
		return new AssetEvent(eventType, entity.getMunicipalityId(), entity.getVersion(), occurredAt, toAsset(entity));
	}

	/**
	 * The asset as it is, as an update at its current version that happened at its last change, so that a consumer that
	 * already has it replaces it with the same. Rows from before created was set have no time of their own.
	 */
	public static AssetEvent toRepublishedAssetEvent(final AssetEntity entity) {
		final var lastChanged = ofNullable(entity.getUpdated())
			.or(() -> ofNullable(entity.getCreated()))
			.orElseGet(() -> now(systemDefault()).truncatedTo(MILLIS));
		return toAssetEvent(entity, UPDATED, lastChanged);
	}

	private static Asset toAsset(final AssetEntity entity) {
		return new Asset(
			entity.getId(),
			entity.getAssetId(),
			entity.getOrigin(),
			entity.getPartyId(),
			entity.getType(),
			entity.getTitle(),
			entity.getDescription(),
			entity.getStatus(),
			entity.getStatusReason(),
			entity.getIssued(),
			entity.getValidTo());
	}
}
