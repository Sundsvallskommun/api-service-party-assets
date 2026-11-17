package se.sundsvall.partyassets.integration.events;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import se.sundsvall.partyassets.api.model.Status;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

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
@Schema(description = "A change of an asset")
public record AssetEvent(
	@Schema(description = "What happened to the asset", requiredMode = REQUIRED) EventType eventType,
	@Schema(description = "The municipality the asset belongs to", examples = "2281") String municipalityId,
	@Schema(description = "The JPA version of the asset after the change. It grows with every change, drafts included, unlike the revision. A deletion carries the version the asset had",
		requiredMode = REQUIRED) Long version,
	@Schema(description = "When the change happened", requiredMode = REQUIRED) OffsetDateTime occurredAt,
	@Schema(description = "The asset after the change, or as it was when it was deleted", requiredMode = REQUIRED) Asset asset) {

	/**
	 * The version of this contract, the info.version of the AsyncAPI document. It is not the service's version. The
	 * Events section of the README says when to bump it.
	 */
	public static final String CONTRACT_VERSION = "1.0";

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
		@Schema(description = "Unique id of asset", examples = "1c8f38a6-b492-4037-b7dc-de5bc6c629f0", requiredMode = REQUIRED) String id,
		@Schema(description = "External asset id (e.g. PRH-123456789) used as an identifier by external systems", examples = "PRH-123456789") String assetId,
		@Schema(description = "Source of origin for the asset", examples = "CASEDATA") String origin,
		@Schema(description = "PartyId", examples = "123e4567-e89b-12d3-a456-426614174000", requiredMode = REQUIRED) String partyId,
		@Schema(description = "Asset type", examples = "PERMIT", requiredMode = REQUIRED) String type,
		@Schema(description = "Asset title, shown to the party", examples = "Stadigvarande tillstånd för servering av alkohol") String title,
		@Schema(description = "Asset description", examples = "Asset description") String description,
		@Schema(description = "Asset status", examples = "ACTIVE") Status status,
		@Schema(description = "Status reason", examples = "Status reason") String statusReason,
		@Schema(description = "Issued date", examples = "2021-01-01", requiredMode = REQUIRED) LocalDate issued,
		@Schema(description = "Valid to date", examples = "2021-12-31") LocalDate validTo) {
	}
}
