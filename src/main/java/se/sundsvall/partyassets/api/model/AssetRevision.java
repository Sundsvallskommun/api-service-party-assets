package se.sundsvall.partyassets.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Schema(description = "An asset as it looked at one point in its history. The newest revision is the asset as it is now")
public class AssetRevision {

	@Schema(description = "Unique id of the asset this revision belongs to", examples = "1c8f38a6-b492-4037-b7dc-de5bc6c629f0", accessMode = READ_ONLY)
	private String id;

	@Schema(description = "Revision number. Numbering starts at 0, which is the asset as it was created, and increases by one for every recorded change",
		examples = "2",
		accessMode = READ_ONLY)
	private Integer revision;

	@Schema(description = "Who created this revision, taken from the X-Sent-By header. Null when the header was absent, and for revisions written by the nightly expiration job",
		examples = "joe01doe",
		accessMode = READ_ONLY)
	private String actor;

	@Schema(description = "When this revision was superseded, or when the asset was last changed for the current revision",
		examples = "2023-01-02T12:00:00+01:00",
		accessMode = READ_ONLY)
	private OffsetDateTime recordedAt;

	@Schema(description = "External asset id (e.g. PRH-123456789) used as an identifier by external systems", examples = "PRH-123456789", accessMode = READ_ONLY)
	private String assetId;

	@Schema(description = "Source of origin for the asset", examples = "CASEDATA", accessMode = READ_ONLY)
	private String origin;

	@Schema(description = "PartyId", examples = "123e4567-e89b-12d3-a456-426614174000", accessMode = READ_ONLY)
	private String partyId;

	@Schema(description = "Asset type", examples = "PERMIT", accessMode = READ_ONLY)
	private String type;

	@Schema(description = "Issued date", examples = "2021-01-01", accessMode = READ_ONLY)
	private LocalDate issued;

	@Schema(description = "Valid to date", examples = "2021-12-31", accessMode = READ_ONLY)
	private LocalDate validTo;

	@Schema(description = "Asset status", examples = "ACTIVE", accessMode = READ_ONLY)
	private Status status;

	@Schema(description = "Status reason", examples = "Status reason", accessMode = READ_ONLY)
	private String statusReason;

	@Schema(description = "Asset description", examples = "Asset description", accessMode = READ_ONLY)
	private String description;

	@Schema(description = "Asset title, shown to the party", examples = "Stadigvarande tillstånd för servering av alkohol", accessMode = READ_ONLY)
	private String title;

	@Schema(description = "Additional parameters", examples = "{\"foo\":\"bar\"}", accessMode = READ_ONLY)
	private Map<String, String> additionalParameters;

	@Schema(description = "JSON parameters", accessMode = READ_ONLY)
	private List<AssetJsonParameter> jsonParameters;

	@Schema(description = "Id of the asset this asset replaces", examples = "1c8f38a6-b492-4037-b7dc-de5bc6c629f0", accessMode = READ_ONLY)
	private String replacesId;

	@Schema(description = "Attachment metadata as it was at this revision. The files themselves never change, so each one is still downloadable by its id even if it has since been removed",
		accessMode = READ_ONLY)
	private List<AssetAttachment> attachments;

	public static AssetRevision create() {
		return new AssetRevision();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public AssetRevision withId(String id) {
		this.id = id;
		return this;
	}

	public Integer getRevision() {
		return revision;
	}

	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	public AssetRevision withRevision(Integer revision) {
		this.revision = revision;
		return this;
	}

	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}

	public AssetRevision withActor(String actor) {
		this.actor = actor;
		return this;
	}

	public OffsetDateTime getRecordedAt() {
		return recordedAt;
	}

	public void setRecordedAt(OffsetDateTime recordedAt) {
		this.recordedAt = recordedAt;
	}

	public AssetRevision withRecordedAt(OffsetDateTime recordedAt) {
		this.recordedAt = recordedAt;
		return this;
	}

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	public AssetRevision withAssetId(String assetId) {
		this.assetId = assetId;
		return this;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public AssetRevision withOrigin(String origin) {
		this.origin = origin;
		return this;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public AssetRevision withPartyId(String partyId) {
		this.partyId = partyId;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public AssetRevision withType(String type) {
		this.type = type;
		return this;
	}

	public LocalDate getIssued() {
		return issued;
	}

	public void setIssued(LocalDate issued) {
		this.issued = issued;
	}

	public AssetRevision withIssued(LocalDate issued) {
		this.issued = issued;
		return this;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	public AssetRevision withValidTo(LocalDate validTo) {
		this.validTo = validTo;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public AssetRevision withStatus(Status status) {
		this.status = status;
		return this;
	}

	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}

	public AssetRevision withStatusReason(String statusReason) {
		this.statusReason = statusReason;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AssetRevision withDescription(String description) {
		this.description = description;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public AssetRevision withTitle(String title) {
		this.title = title;
		return this;
	}

	public Map<String, String> getAdditionalParameters() {
		return additionalParameters;
	}

	public void setAdditionalParameters(Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
	}

	public AssetRevision withAdditionalParameters(Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
		return this;
	}

	public List<AssetJsonParameter> getJsonParameters() {
		return jsonParameters;
	}

	public void setJsonParameters(List<AssetJsonParameter> jsonParameters) {
		this.jsonParameters = jsonParameters;
	}

	public AssetRevision withJsonParameters(List<AssetJsonParameter> jsonParameters) {
		this.jsonParameters = jsonParameters;
		return this;
	}

	public String getReplacesId() {
		return replacesId;
	}

	public void setReplacesId(String replacesId) {
		this.replacesId = replacesId;
	}

	public AssetRevision withReplacesId(String replacesId) {
		this.replacesId = replacesId;
		return this;
	}

	public List<AssetAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AssetAttachment> attachments) {
		this.attachments = attachments;
	}

	public AssetRevision withAttachments(List<AssetAttachment> attachments) {
		this.attachments = attachments;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(actor, additionalParameters, assetId, attachments, description, title, id, issued, jsonParameters, origin, partyId, recordedAt, replacesId, revision, status, statusReason, type, validTo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AssetRevision other = (AssetRevision) obj;
		return Objects.equals(actor, other.actor) && Objects.equals(additionalParameters, other.additionalParameters) && Objects.equals(assetId, other.assetId) && Objects.equals(attachments, other.attachments) && Objects.equals(description,
			other.description) && Objects.equals(title, other.title) && Objects.equals(id, other.id) && Objects.equals(issued, other.issued) && Objects.equals(jsonParameters, other.jsonParameters) && Objects.equals(origin, other.origin) && Objects.equals(
				partyId, other.partyId)
			&& Objects.equals(recordedAt, other.recordedAt) && Objects.equals(replacesId, other.replacesId) && Objects.equals(revision, other.revision) && status == other.status && Objects.equals(statusReason, other.statusReason) && Objects.equals(type,
				other.type) && Objects.equals(validTo, other.validTo);
	}

	@Override
	public String toString() {
		return "AssetRevision [id=" + id + ", revision=" + revision + ", actor=" + actor + ", recordedAt=" + recordedAt + ", assetId=" + assetId + ", origin=" + origin + ", partyId=" + partyId + ", type=" + type + ", issued=" + issued + ", validTo="
			+ validTo + ", status=" + status + ", statusReason=" + statusReason + ", description=" + description + ", title=" + title + ", additionalParameters=" + additionalParameters + ", jsonParameters=" + jsonParameters + ", replacesId=" + replacesId
			+ ", attachments="
			+ attachments + "]";
	}
}
