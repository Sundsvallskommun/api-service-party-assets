package se.sundsvall.partyassets.integration.db.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import se.sundsvall.partyassets.api.model.Status;

@Entity
@Table(name = "asset", uniqueConstraints = { @UniqueConstraint(name = "uc_asset_asset_id", columnNames = { "asset_id" }) })
public class AssetEntity {

	@Id
	@UuidGenerator
	private String id;

	@Column(name = "asset_id")
	private String assetId;

	private String partyId;

	@ElementCollection(fetch = EAGER)
	@CollectionTable(name = "case_reference_id", joinColumns = @JoinColumn(name = "asset_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_case_reference_id_asset_id")), indexes = {
		@Index(name = "idx_case_reference_id_asset_id", columnList = "asset_id")
	})
	@Column(name = "case_reference_id", nullable = false)
	private List<String> caseReferenceIds;

	@Column(name = "`type`")
	private String type;

	private LocalDate issued;

	private LocalDate validTo;

	@Enumerated(STRING)
	private Status status;

	private String statusReason;

	private String description;

	@ElementCollection(fetch = EAGER)
	@CollectionTable(name = "additional_parameter", joinColumns = @JoinColumn(name = "asset_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_additional_parameter_asset_id")),
		indexes = {
			@Index(name = "idx_additional_parameter_asset_id", columnList = "asset_id")
		})
	@MapKeyColumn(name = "parameter_key")
	@Column(name = "parameter_value", nullable = false)
	private Map<String, String> additionalParameters;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime created;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime updated;

	public static AssetEntity create() {
		return new AssetEntity();
	}

	@PrePersist
	void prePersist() {
		created = now(ZoneId.systemDefault()).truncatedTo(MILLIS);
	}

	@PreUpdate()
	void preUpdate() {
		updated = now(ZoneId.systemDefault()).truncatedTo(MILLIS);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public AssetEntity withId(String id) {
		this.id = id;
		return this;
	}

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	public AssetEntity withAssetId(String assetId) {
		this.assetId = assetId;
		return this;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public AssetEntity withPartyId(String partyId) {
		this.partyId = partyId;
		return this;
	}

	public List<String> getCaseReferenceIds() {
		return caseReferenceIds;
	}

	public void setCaseReferenceIds(List<String> caseReferenceIds) {
		this.caseReferenceIds = caseReferenceIds;
	}

	public AssetEntity withCaseReferenceIds(List<String> caseReferenceIds) {
		this.caseReferenceIds = caseReferenceIds;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public AssetEntity withType(String type) {
		this.type = type;
		return this;
	}

	public LocalDate getIssued() {
		return issued;
	}

	public void setIssued(LocalDate issued) {
		this.issued = issued;
	}

	public AssetEntity withIssued(LocalDate issued) {
		this.issued = issued;
		return this;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	public AssetEntity withValidTo(LocalDate validTo) {
		this.validTo = validTo;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public AssetEntity withStatus(Status status) {
		this.status = status;
		return this;
	}

	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}

	public AssetEntity withStatusReason(String statusReason) {
		this.statusReason = statusReason;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AssetEntity withDescription(String description) {
		this.description = description;
		return this;
	}

	public Map<String, String> getAdditionalParameters() {
		return additionalParameters;
	}

	public void setAdditionalParameters(Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
	}

	public AssetEntity withAdditionalParameters(Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	public AssetEntity withCreated(OffsetDateTime created) {
		this.created = created;
		return this;
	}

	public OffsetDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(OffsetDateTime updated) {
		this.updated = updated;
	}

	public AssetEntity withUpdated(OffsetDateTime updated) {
		this.updated = updated;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(additionalParameters, assetId, caseReferenceIds, created, description, id, issued, partyId, status, statusReason, type, updated, validTo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AssetEntity)) {
			return false;
		}
		AssetEntity other = (AssetEntity) obj;
		return Objects.equals(additionalParameters, other.additionalParameters) && Objects.equals(assetId, other.assetId) && Objects.equals(caseReferenceIds, other.caseReferenceIds) && Objects.equals(created, other.created) && Objects.equals(
			description, other.description) && Objects.equals(id, other.id) && Objects.equals(issued, other.issued) && Objects.equals(partyId, other.partyId) && status == other.status && Objects.equals(statusReason, other.statusReason) && Objects
				.equals(type, other.type) && Objects.equals(updated, other.updated) && Objects.equals(validTo, other.validTo);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AssetEntity [id=").append(id).append(", assetId=").append(assetId).append(", partyId=").append(partyId).append(", caseReferenceIds=").append(caseReferenceIds).append(", type=").append(type).append(", issued=").append(issued)
			.append(", validTo=").append(validTo).append(", status=").append(status).append(", statusReason=").append(statusReason).append(", description=").append(description).append(", additionalParameters=").append(additionalParameters).append(
				", created=").append(created).append(", updated=").append(updated).append("]");
		return builder.toString();
	}
}
