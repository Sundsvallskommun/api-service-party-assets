package se.sundsvall.partyassets.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Asset {

	@Schema(description = "Unique id of asset", examples = "1c8f38a6-b492-4037-b7dc-de5bc6c629f0")
	private String id;

	@Schema(description = "External asset id", examples = "PRH-123456789")
	private String assetId;

	@Schema(description = "Source of origin for the asset", examples = "CASEDATA")
	private String origin;

	@Schema(description = "PartyId", examples = "123e4567-e89b-12d3-a456-426614174000")
	private String partyId;

	@Schema(description = "Case reference ids", examples = "[\"945576d3-6e92-4118-ba33-53582d338ad3\"]")
	private List<String> caseReferenceIds;

	@Schema(description = "Asset type", examples = "PERMIT")
	private String type;

	@Schema(description = "Issued date", examples = "2021-01-01")
	private LocalDate issued;

	@Schema(description = "Valid to date", examples = "2021-12-31")
	private LocalDate validTo;

	@Schema(description = "Asset status", examples = "ACTIVE")
	private Status status;

	@Schema(description = "Status reason", examples = "Status reason")
	private String statusReason;

	@Schema(description = "Asset description", examples = "Asset description")
	private String description;

	@Schema(description = "Additional parameters", examples = "{\"foo\":\"bar\"}")
	private Map<String, String> additionalParameters;

	@Schema(description = "JSON parameters")
	private List<AssetJsonParameter> jsonParameters;

	public static Asset create() {
		return new Asset();
	}

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	public Asset withAssetId(String assetId) {
		this.assetId = assetId;
		return this;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Asset withId(String id) {
		this.id = id;
		return this;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public Asset withOrigin(String origin) {
		this.origin = origin;
		return this;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public Asset withPartyId(String partyId) {
		this.partyId = partyId;
		return this;
	}

	public List<String> getCaseReferenceIds() {
		return caseReferenceIds;
	}

	public void setCaseReferenceIds(List<String> caseReferenceIds) {
		this.caseReferenceIds = caseReferenceIds;
	}

	public Asset withCaseReferenceIds(List<String> caseReferenceIds) {
		this.caseReferenceIds = caseReferenceIds;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Asset withType(String type) {
		this.type = type;
		return this;
	}

	public LocalDate getIssued() {
		return issued;
	}

	public void setIssued(LocalDate issued) {
		this.issued = issued;
	}

	public Asset withIssued(LocalDate issued) {
		this.issued = issued;
		return this;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	public Asset withValidTo(LocalDate validTo) {
		this.validTo = validTo;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Asset withStatus(Status status) {
		this.status = status;
		return this;
	}

	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}

	public Asset withStatusReason(String statusReason) {
		this.statusReason = statusReason;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Asset withDescription(String description) {
		this.description = description;
		return this;
	}

	public Map<String, String> getAdditionalParameters() {
		return additionalParameters;
	}

	public void setAdditionalParameters(Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
	}

	public Asset withAdditionalParameters(Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
		return this;
	}

	public List<AssetJsonParameter> getJsonParameters() {
		return jsonParameters;
	}

	public void setJsonParameters(List<AssetJsonParameter> jsonParameters) {
		this.jsonParameters = jsonParameters;
	}

	public Asset withJsonParameters(List<AssetJsonParameter> jsonParameters) {
		this.jsonParameters = jsonParameters;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(additionalParameters, assetId, caseReferenceIds, description, id, issued, jsonParameters, origin, partyId, status, statusReason, type, validTo);
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
		Asset other = (Asset) obj;
		return Objects.equals(additionalParameters, other.additionalParameters) && Objects.equals(assetId, other.assetId) && Objects.equals(caseReferenceIds, other.caseReferenceIds) && Objects.equals(description, other.description) && Objects.equals(id,
			other.id) && Objects.equals(issued, other.issued) && Objects.equals(jsonParameters, other.jsonParameters) && Objects.equals(origin, other.origin) && Objects.equals(partyId, other.partyId) && status == other.status && Objects.equals(
				statusReason, other.statusReason) && Objects.equals(type, other.type) && Objects.equals(validTo, other.validTo);
	}

	@Override
	public String toString() {
		return "Asset [id=" + id + ", assetId=" + assetId + ", origin=" + origin + ", partyId=" + partyId + ", caseReferenceIds=" + caseReferenceIds + ", type=" + type + ", issued=" + issued + ", validTo=" + validTo + ", status=" + status
			+ ", statusReason=" + statusReason + ", description=" + description + ", additionalParameters=" + additionalParameters + ", jsonParameters=" + jsonParameters + "]";
	}
}
