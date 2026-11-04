package se.sundsvall.partyassets.integration.events;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import se.sundsvall.partyassets.api.model.Status;

/**
 * A change of an asset, as published on RabbitMQ. Its JSON is the contract with the consumers, so a field is not
 * renamed or removed without them.
 *
 * @param eventType      what happened to the asset
 * @param municipalityId the municipality the asset belongs to
 * @param version        the JPA version of the asset after the change. It grows with every change, drafts included,
 *                       unlike the revision. A deletion carries the version the asset had
 * @param occurredAt     when the change happened
 * @param asset          the asset after the change, or as it was when it was deleted
 */
public record AssetEvent(EventType eventType, String municipalityId, Long version, OffsetDateTime occurredAt, Asset asset) {

	public enum EventType {
		CREATED("asset.created"),
		UPDATED("asset.updated"),
		DELETED("asset.deleted");

		private final String routingKey;

		EventType(final String routingKey) {
			this.routingKey = routingKey;
		}

		public String getRoutingKey() {
			return routingKey;
		}
	}

	public record Asset(
		String id,
		String assetId,
		String origin,
		String partyId,
		String type,
		String title,
		String description,
		Status status,
		String statusReason,
		LocalDate issued,
		LocalDate validTo) {
	}
}
