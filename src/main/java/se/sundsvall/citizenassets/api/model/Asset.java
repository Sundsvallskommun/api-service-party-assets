package se.sundsvall.citizenassets.api.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

public class Asset {

	@Schema(description = "Unique id of asset", example = "1c8f38a6-b492-4037-b7dc-de5bc6c629f0")
	private String id;

	@Schema(description = "External asset id", example = "PRH-123456789")
	private String assetId;

	@Schema(description = "PartyId", example = "123e4567-e89b-12d3-a456-426614174000")
	private String partyId;

	@Schema(description = "Case reference ids", example = "[\"945576d3-6e92-4118-ba33-53582d338ad3\"]")
	private List<String> caseReferenceIds;

	@Schema(description = "Asset type", example = "PERMIT")
	private String type;

	@Schema(description = "Issued date", example = "2021-01-01")
	private LocalDate issued;

	@Schema(description = "Valid to date", example = "2021-12-31")
	private LocalDate validTo;

	@Schema(description = "Asset status", example = "ACTIVE")
	private Status status;

	@Schema(description = "Status reason", example = "Status reason")
	private String statusReason;

	@Schema(description = "Asset description", example = "Asset description")
	private String description;

	@Schema(description = "Additional parameters", example = "{\"foo\":\"bar\"}")
	private Map<String, String> additionalParameters;

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

	@Override
	public int hashCode() {
		return Objects.hash(additionalParameters, assetId, caseReferenceIds, description, id, issued, partyId, status, statusReason, type, validTo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Asset)) {
			return false;
		}
		Asset other = (Asset) obj;
		return Objects.equals(additionalParameters, other.additionalParameters) && Objects.equals(assetId, other.assetId) && Objects.equals(caseReferenceIds, other.caseReferenceIds) && Objects.equals(description, other.description) && Objects.equals(
			id, other.id) && Objects.equals(issued, other.issued) && Objects.equals(partyId, other.partyId) && status == other.status && Objects.equals(statusReason, other.statusReason) && Objects.equals(type, other.type) && Objects.equals(validTo,
				other.validTo);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Asset [id=").append(id).append(", assetId=").append(assetId).append(", partyId=").append(partyId).append(", caseReferenceIds=").append(caseReferenceIds).append(", type=").append(type).append(", issued=").append(issued).append(
			", validTo=").append(validTo).append(", status=").append(status).append(", statusReason=").append(statusReason).append(", description=").append(description).append(", additionalParameters=").append(additionalParameters).append("]");
		return builder.toString();
	}
}
