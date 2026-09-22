package se.sundsvall.partyassets.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.UuidGenerator;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.hibernate.Length.LONG32;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

/**
 * A snapshot of an asset as it was before a change. The current state stays on AssetEntity; everything older lives
 * here, one row per revision. See docs/design-revisionshantering.md.
 */
@Entity
@Table(name = "asset_revision",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_asset_revision_asset_id_revision", columnNames = {
			"asset_id", "revision"
		})
	})
public class AssetRevisionEntity {

	@Id
	@UuidGenerator
	private String id;

	// The asset primary key, deliberately not mapped as an association: an association would pull AssetEntity's eager
	// collections into every revision read, and nothing is meant to join out of this table. The cascading delete is
	// therefore expressed in the migration rather than by JPA.
	@Column(name = "asset_id", nullable = false)
	private String assetId;

	@Column(name = "revision", nullable = false)
	private Integer revision;

	@Column(name = "actor")
	private String actor;

	@TimeZoneStorage(NORMALIZE)
	@Column(name = "recorded_at", nullable = false)
	private OffsetDateTime recordedAt;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "origin")
	private String origin;

	// AssetEntity.assetId, the number the business uses. Renamed here because asset_id already means the primary key.
	@Column(name = "external_asset_id")
	private String externalAssetId;

	@Column(name = "party_id")
	private String partyId;

	// Status and party type are stored as plain strings rather than as enums. Hibernate renders an @Enumerated column
	// as a real MariaDB enum, which would reject historical rows the day a value is dropped from the Java enum.
	@Column(name = "party_type", length = 32)
	private String partyType;

	@Column(name = "`type`")
	private String type;

	@Column(name = "issued")
	private LocalDate issued;

	@Column(name = "valid_to")
	private LocalDate validTo;

	@Column(name = "replaces_id")
	private String replacesId;

	@Column(name = "status", length = 32)
	private String status;

	@Column(name = "status_reason")
	private String statusReason;

	@Column(name = "description")
	private String description;

	@Column(name = "additional_parameters", length = LONG32)
	private String additionalParameters;

	@Column(name = "case_reference_ids", length = LONG32)
	private String caseReferenceIds;

	@Column(name = "json_parameters", length = LONG32)
	private String jsonParameters;

	@Column(name = "attachments", length = LONG32)
	private String attachments;

	public static AssetRevisionEntity create() {
		return new AssetRevisionEntity();
	}

	@PrePersist
	void prePersist() {
		recordedAt = now(systemDefault()).truncatedTo(MILLIS);
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public AssetRevisionEntity withId(final String id) {
		this.id = id;
		return this;
	}

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(final String assetId) {
		this.assetId = assetId;
	}

	public AssetRevisionEntity withAssetId(final String assetId) {
		this.assetId = assetId;
		return this;
	}

	public Integer getRevision() {
		return revision;
	}

	public void setRevision(final Integer revision) {
		this.revision = revision;
	}

	public AssetRevisionEntity withRevision(final Integer revision) {
		this.revision = revision;
		return this;
	}

	public String getActor() {
		return actor;
	}

	public void setActor(final String actor) {
		this.actor = actor;
	}

	public AssetRevisionEntity withActor(final String actor) {
		this.actor = actor;
		return this;
	}

	public OffsetDateTime getRecordedAt() {
		return recordedAt;
	}

	public void setRecordedAt(final OffsetDateTime recordedAt) {
		this.recordedAt = recordedAt;
	}

	public AssetRevisionEntity withRecordedAt(final OffsetDateTime recordedAt) {
		this.recordedAt = recordedAt;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public AssetRevisionEntity withMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(final String origin) {
		this.origin = origin;
	}

	public AssetRevisionEntity withOrigin(final String origin) {
		this.origin = origin;
		return this;
	}

	public String getExternalAssetId() {
		return externalAssetId;
	}

	public void setExternalAssetId(final String externalAssetId) {
		this.externalAssetId = externalAssetId;
	}

	public AssetRevisionEntity withExternalAssetId(final String externalAssetId) {
		this.externalAssetId = externalAssetId;
		return this;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(final String partyId) {
		this.partyId = partyId;
	}

	public AssetRevisionEntity withPartyId(final String partyId) {
		this.partyId = partyId;
		return this;
	}

	public String getPartyType() {
		return partyType;
	}

	public void setPartyType(final String partyType) {
		this.partyType = partyType;
	}

	public AssetRevisionEntity withPartyType(final String partyType) {
		this.partyType = partyType;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public AssetRevisionEntity withType(final String type) {
		this.type = type;
		return this;
	}

	public LocalDate getIssued() {
		return issued;
	}

	public void setIssued(final LocalDate issued) {
		this.issued = issued;
	}

	public AssetRevisionEntity withIssued(final LocalDate issued) {
		this.issued = issued;
		return this;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(final LocalDate validTo) {
		this.validTo = validTo;
	}

	public AssetRevisionEntity withValidTo(final LocalDate validTo) {
		this.validTo = validTo;
		return this;
	}

	public String getReplacesId() {
		return replacesId;
	}

	public void setReplacesId(final String replacesId) {
		this.replacesId = replacesId;
	}

	public AssetRevisionEntity withReplacesId(final String replacesId) {
		this.replacesId = replacesId;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public AssetRevisionEntity withStatus(final String status) {
		this.status = status;
		return this;
	}

	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(final String statusReason) {
		this.statusReason = statusReason;
	}

	public AssetRevisionEntity withStatusReason(final String statusReason) {
		this.statusReason = statusReason;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public AssetRevisionEntity withDescription(final String description) {
		this.description = description;
		return this;
	}

	public String getAdditionalParameters() {
		return additionalParameters;
	}

	public void setAdditionalParameters(final String additionalParameters) {
		this.additionalParameters = additionalParameters;
	}

	public AssetRevisionEntity withAdditionalParameters(final String additionalParameters) {
		this.additionalParameters = additionalParameters;
		return this;
	}

	public String getCaseReferenceIds() {
		return caseReferenceIds;
	}

	public void setCaseReferenceIds(final String caseReferenceIds) {
		this.caseReferenceIds = caseReferenceIds;
	}

	public AssetRevisionEntity withCaseReferenceIds(final String caseReferenceIds) {
		this.caseReferenceIds = caseReferenceIds;
		return this;
	}

	public String getJsonParameters() {
		return jsonParameters;
	}

	public void setJsonParameters(final String jsonParameters) {
		this.jsonParameters = jsonParameters;
	}

	public AssetRevisionEntity withJsonParameters(final String jsonParameters) {
		this.jsonParameters = jsonParameters;
		return this;
	}

	public String getAttachments() {
		return attachments;
	}

	public void setAttachments(final String attachments) {
		this.attachments = attachments;
	}

	public AssetRevisionEntity withAttachments(final String attachments) {
		this.attachments = attachments;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(actor, additionalParameters, assetId, attachments, caseReferenceIds, description, externalAssetId, id, issued, jsonParameters, municipalityId, origin, partyId, partyType, recordedAt, replacesId, revision, status,
			statusReason, type, validTo);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AssetRevisionEntity other = (AssetRevisionEntity) obj;
		return Objects.equals(actor, other.actor) && Objects.equals(additionalParameters, other.additionalParameters) && Objects.equals(assetId, other.assetId) && Objects.equals(attachments, other.attachments) && Objects.equals(caseReferenceIds,
			other.caseReferenceIds) && Objects.equals(description, other.description) && Objects.equals(externalAssetId, other.externalAssetId) && Objects.equals(id, other.id) && Objects.equals(issued, other.issued) && Objects.equals(jsonParameters,
				other.jsonParameters) && Objects.equals(municipalityId, other.municipalityId) && Objects.equals(origin, other.origin) && Objects.equals(partyId, other.partyId) && Objects.equals(partyType, other.partyType) && Objects.equals(recordedAt,
					other.recordedAt) && Objects.equals(replacesId, other.replacesId) && Objects.equals(revision, other.revision) && Objects.equals(status, other.status) && Objects.equals(statusReason, other.statusReason) && Objects.equals(type,
						other.type) && Objects.equals(validTo, other.validTo);
	}

	@Override
	public String toString() {
		return "AssetRevisionEntity [id=" + id + ", assetId=" + assetId + ", revision=" + revision + ", actor=" + actor + ", recordedAt=" + recordedAt + ", municipalityId=" + municipalityId + ", origin=" + origin + ", externalAssetId=" + externalAssetId
			+ ", partyId=" + partyId + ", partyType=" + partyType + ", type=" + type + ", issued=" + issued + ", validTo=" + validTo + ", replacesId=" + replacesId + ", status=" + status + ", statusReason=" + statusReason + ", description=" + description
			+ ", additionalParameters=" + additionalParameters + ", caseReferenceIds=" + caseReferenceIds + ", jsonParameters=" + jsonParameters + ", attachments=" + attachments + "]";
	}
}
