package se.sundsvall.partyassets.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.partyassets.api.validation.ValidJsonParameter;
import se.sundsvall.partyassets.api.validation.ValidStatusReason;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@ValidStatusReason
public class AssetCreateRequest {

	@NotEmpty
	@Schema(description = "Asset id", examples = "PRH-123456789", requiredMode = REQUIRED)
	private String assetId;

	@Schema(description = "Source of origin for the asset", examples = "CASEDATA")
	private String origin;

	@ValidUuid
	@Schema(description = "PartyId", examples = "123e4567-e89b-12d3-a456-426614174000", requiredMode = REQUIRED)
	private String partyId;

	@Schema(description = "Case reference ids", examples = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
	private List<String> caseReferenceIds;

	@NotEmpty
	@Schema(description = "Asset type", examples = "PERMIT", requiredMode = REQUIRED)
	private String type;

	@NotNull
	@Schema(description = "Issued date", examples = "2021-01-01", requiredMode = REQUIRED)
	private LocalDate issued;

	@Schema(description = "Valid to date", examples = "2021-12-31")
	private LocalDate validTo;

	@NotNull
	@Schema(description = "Asset status", examples = "ACTIVE", requiredMode = REQUIRED)
	private Status status;

	@Schema(description = "Status reason", examples = "Status reason")
	private String statusReason;

	@Schema(description = "Asset description", examples = "Asset description")
	private String description;

	@Schema(description = "Additional parameters", examples = "{\"foo\":\"bar\"}")
	private Map<String, String> additionalParameters;

	@Schema(description = "JSON parameters")
	private List<@ValidJsonParameter AssetJsonParameter> jsonParameters;

	public static AssetCreateRequest create() {
		return new AssetCreateRequest();
	}

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	public AssetCreateRequest withAssetId(String assetId) {
		this.assetId = assetId;
		return this;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public AssetCreateRequest withOrigin(String origin) {
		this.origin = origin;
		return this;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public AssetCreateRequest withPartyId(String partyId) {
		this.partyId = partyId;
		return this;
	}

	public List<String> getCaseReferenceIds() {
		return caseReferenceIds;
	}

	public void setCaseReferenceIds(List<String> caseReferenceIds) {
		this.caseReferenceIds = caseReferenceIds;
	}

	public AssetCreateRequest withCaseReferenceIds(List<String> caseReferenceIds) {
		this.caseReferenceIds = caseReferenceIds;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public AssetCreateRequest withType(String type) {
		this.type = type;
		return this;
	}

	public LocalDate getIssued() {
		return issued;
	}

	public void setIssued(LocalDate issued) {
		this.issued = issued;
	}

	public AssetCreateRequest withIssued(LocalDate issued) {
		this.issued = issued;
		return this;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	public AssetCreateRequest withValidTo(LocalDate validTo) {
		this.validTo = validTo;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public AssetCreateRequest withStatus(Status status) {
		this.status = status;
		return this;
	}

	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}

	public AssetCreateRequest withStatusReason(String statusReason) {
		this.statusReason = statusReason;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AssetCreateRequest withDescription(String description) {
		this.description = description;
		return this;
	}

	public Map<String, String> getAdditionalParameters() {
		return additionalParameters;
	}

	public void setAdditionalParameter(String key, String value) {
		if (additionalParameters == null) {
			additionalParameters = new HashMap<>();
		}
		additionalParameters.put(key, value);
	}

	public void setAdditionalParameters(Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
	}

	public AssetCreateRequest withAdditionalParameters(Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
		return this;
	}

	public List<AssetJsonParameter> getJsonParameters() {
		return jsonParameters;
	}

	public void setJsonParameters(List<AssetJsonParameter> jsonParameters) {
		this.jsonParameters = jsonParameters;
	}

	public AssetCreateRequest withJsonParameters(List<AssetJsonParameter> jsonParameters) {
		this.jsonParameters = jsonParameters;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(additionalParameters, assetId, caseReferenceIds, description, issued, jsonParameters, origin, partyId, status, statusReason, type, validTo);
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
		AssetCreateRequest other = (AssetCreateRequest) obj;
		return Objects.equals(additionalParameters, other.additionalParameters) && Objects.equals(assetId, other.assetId) && Objects.equals(caseReferenceIds, other.caseReferenceIds) && Objects.equals(description, other.description) && Objects.equals(
			issued, other.issued) && Objects.equals(jsonParameters, other.jsonParameters) && Objects.equals(origin, other.origin) && Objects.equals(partyId, other.partyId) && status == other.status && Objects.equals(statusReason, other.statusReason)
			&& Objects.equals(type, other.type) && Objects.equals(validTo, other.validTo);
	}

	@Override
	public String toString() {
		return "AssetCreateRequest [assetId=" + assetId + ", origin=" + origin + ", partyId=" + partyId + ", caseReferenceIds=" + caseReferenceIds + ", type=" + type + ", issued=" + issued + ", validTo=" + validTo + ", status=" + status + ", statusReason="
			+ statusReason + ", description=" + description + ", additionalParameters=" + additionalParameters + ", jsonParameters=" + jsonParameters + "]";
	}
}
