package se.sundsvall.partyassets.integration.db.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.UuidGenerator;
import se.sundsvall.partyassets.api.model.Status;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

@Entity
@Table(name = "asset",
	indexes = {
		@Index(name = "idx_asset_municipality_id", columnList = "municipality_id")
	})
public class AssetEntity {

	@Id
	@UuidGenerator
	private String id;

	@Column(name = "municipality_id")
	private String municipalityId;

	private String origin;

	@Column(name = "asset_id")
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
	@CollectionTable(name = "additional_parameter",
		joinColumns = @JoinColumn(name = "asset_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_additional_parameter_asset_id")),
		indexes = {
			@Index(name = "idx_additional_parameter_asset_id", columnList = "asset_id")
		})
	@MapKeyColumn(name = "parameter_key")
	@Column(name = "parameter_value", nullable = false)
	private Map<String, String> additionalParameters;

	@OneToMany(fetch = EAGER, mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AssetJsonParameterEntity> jsonParameters;

	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

	public static AssetEntity create() {
		return new AssetEntity();
	}

	@PrePersist
	void prePersist() {
		created = now(systemDefault()).truncatedTo(MILLIS);
	}

	@PreUpdate()
	void preUpdate() {
		updated = now(systemDefault()).truncatedTo(MILLIS);
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

	public List<AssetJsonParameterEntity> getJsonParameters() {
		return jsonParameters;
	}

	public void setJsonParameters(List<AssetJsonParameterEntity> jsonParameters) {
		this.jsonParameters = jsonParameters;
	}

	public AssetEntity withJsonParameters(List<AssetJsonParameterEntity> jsonParameters) {
		this.jsonParameters = jsonParameters;
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

	public AssetEntity addOrReplaceJsonParameters(List<AssetJsonParameterEntity> jsonParameters) {
		if (this.jsonParameters == null) {
			this.jsonParameters = new ArrayList<>();
		}

		final var safeList = ofNullable(jsonParameters).orElse(emptyList());
		safeList.forEach(p -> p.setAsset(this));

		this.jsonParameters.clear();
		this.jsonParameters.addAll(safeList);

		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(additionalParameters, assetId, caseReferenceIds, created, description, id, issued, jsonParameters, municipalityId, origin, partyId, partyType, status, statusReason, type, updated, validTo);
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
		AssetEntity other = (AssetEntity) obj;
		return Objects.equals(additionalParameters, other.additionalParameters) && Objects.equals(assetId, other.assetId) && Objects.equals(caseReferenceIds, other.caseReferenceIds) && Objects.equals(created, other.created) && Objects.equals(description,
			other.description) && Objects.equals(id, other.id) && Objects.equals(issued, other.issued) && Objects.equals(jsonParameters, other.jsonParameters) && Objects.equals(municipalityId, other.municipalityId) && Objects.equals(origin, other.origin)
			&& Objects.equals(partyId, other.partyId) && partyType == other.partyType && status == other.status && Objects.equals(statusReason, other.statusReason) && Objects.equals(type, other.type) && Objects.equals(updated, other.updated) && Objects
				.equals(validTo, other.validTo);
	}

	@Override
	public String toString() {
		return "AssetEntity [id=" + id + ", municipalityId=" + municipalityId + ", origin=" + origin + ", assetId=" + assetId + ", partyId=" + partyId + ", partyType=" + partyType + ", caseReferenceIds=" + caseReferenceIds + ", type=" + type + ", issued="
			+ issued + ", validTo=" + validTo + ", status=" + status + ", statusReason=" + statusReason + ", description=" + description + ", additionalParameters=" + additionalParameters + ", jsonParameters=" + jsonParameters + ", created=" + created
			+ ", updated=" + updated + "]";
	}
}
