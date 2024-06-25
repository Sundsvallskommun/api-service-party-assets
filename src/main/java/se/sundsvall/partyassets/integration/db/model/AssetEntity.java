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

import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.hibernate.annotations.UuidGenerator;

import se.sundsvall.partyassets.api.model.Status;

@Entity
@Table(name = "asset", uniqueConstraints = {@UniqueConstraint(name = "uc_asset_asset_id", columnNames = {"asset_id"})})
public class AssetEntity {

	@Id
	@UuidGenerator
	private String id;

	@Column(name = "municipality_id")
	private String municipalityId;

	private String origin;

	@Column(name = "asset_id", nullable = false)
	private String assetId;

	@Column(name = "party_id", nullable = false)
	private String partyId;

	@Enumerated(STRING)
	private PartyType partyType;

	@ElementCollection(fetch = EAGER)
	@CollectionTable(name = "case_reference_id", joinColumns = @JoinColumn(name = "asset_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_case_reference_id_asset_id")), indexes = {
		@Index(name = "idx_case_reference_id_asset_id", columnList = "asset_id")
	})
	@Column(name = "case_reference_id", nullable = false)
	private List<String> caseReferenceIds;

	@Column(name = "`type`", nullable = false)
	private String type;

	@Column(name = "issued", nullable = false)
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

	public void setId(final String id) {
		this.id = id;
	}

	public AssetEntity withId(final String id) {
		this.id = id;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public AssetEntity withMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(final String origin) {
		this.origin = origin;
	}

	public AssetEntity withOrigin(final String origin) {
		this.origin = origin;
		return this;
	}

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(final String assetId) {
		this.assetId = assetId;
	}

	public AssetEntity withAssetId(final String assetId) {
		this.assetId = assetId;
		return this;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(final String partyId) {
		this.partyId = partyId;
	}

	public AssetEntity withPartyId(final String partyId) {
		this.partyId = partyId;
		return this;
	}

	public PartyType getPartyType() {
		return partyType;
	}

	public void setPartyType(final PartyType partyType) {
		this.partyType = partyType;
	}

	public AssetEntity withPartyType(final PartyType partyType) {
		this.partyType = partyType;
		return this;
	}

	public List<String> getCaseReferenceIds() {
		return caseReferenceIds;
	}

	public void setCaseReferenceIds(final List<String> caseReferenceIds) {
		this.caseReferenceIds = caseReferenceIds;
	}

	public AssetEntity withCaseReferenceIds(final List<String> caseReferenceIds) {
		this.caseReferenceIds = caseReferenceIds;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public AssetEntity withType(final String type) {
		this.type = type;
		return this;
	}

	public LocalDate getIssued() {
		return issued;
	}

	public void setIssued(final LocalDate issued) {
		this.issued = issued;
	}

	public AssetEntity withIssued(final LocalDate issued) {
		this.issued = issued;
		return this;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(final LocalDate validTo) {
		this.validTo = validTo;
	}

	public AssetEntity withValidTo(final LocalDate validTo) {
		this.validTo = validTo;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(final Status status) {
		this.status = status;
	}

	public AssetEntity withStatus(final Status status) {
		this.status = status;
		return this;
	}

	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(final String statusReason) {
		this.statusReason = statusReason;
	}

	public AssetEntity withStatusReason(final String statusReason) {
		this.statusReason = statusReason;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public AssetEntity withDescription(final String description) {
		this.description = description;
		return this;
	}

	public Map<String, String> getAdditionalParameters() {
		return additionalParameters;
	}

	public void setAdditionalParameters(final Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
	}

	public AssetEntity withAdditionalParameters(final Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(final OffsetDateTime created) {
		this.created = created;
	}

	public AssetEntity withCreated(final OffsetDateTime created) {
		this.created = created;
		return this;
	}

	public OffsetDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(final OffsetDateTime updated) {
		this.updated = updated;
	}

	public AssetEntity withUpdated(final OffsetDateTime updated) {
		this.updated = updated;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final AssetEntity that = (AssetEntity) o;
		return Objects.equals(id, that.id) && Objects.equals(municipalityId, that.municipalityId) && Objects.equals(origin, that.origin) && Objects.equals(assetId, that.assetId) && Objects.equals(partyId, that.partyId) && partyType == that.partyType && Objects.equals(caseReferenceIds, that.caseReferenceIds) && Objects.equals(type, that.type) && Objects.equals(issued, that.issued) && Objects.equals(validTo, that.validTo) && status == that.status && Objects.equals(statusReason, that.statusReason) && Objects.equals(description, that.description) && Objects.equals(additionalParameters, that.additionalParameters) && Objects.equals(created, that.created) && Objects.equals(updated, that.updated);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, municipalityId, origin, assetId, partyId, partyType, caseReferenceIds, type, issued, validTo, status, statusReason, description, additionalParameters, created, updated);
	}

	@Override
	public String toString() {
		return "AssetEntity{" +
			"id='" + id + '\'' +
			", municipalityId='" + municipalityId + '\'' +
			", origin='" + origin + '\'' +
			", assetId='" + assetId + '\'' +
			", partyId='" + partyId + '\'' +
			", partyType=" + partyType +
			", caseReferenceIds=" + caseReferenceIds +
			", type='" + type + '\'' +
			", issued=" + issued +
			", validTo=" + validTo +
			", status=" + status +
			", statusReason='" + statusReason + '\'' +
			", description='" + description + '\'' +
			", additionalParameters=" + additionalParameters +
			", created=" + created +
			", updated=" + updated +
			'}';
	}

}
