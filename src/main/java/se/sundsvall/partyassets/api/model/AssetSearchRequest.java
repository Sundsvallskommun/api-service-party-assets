package se.sundsvall.partyassets.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

public class AssetSearchRequest {

	@ValidUuid
	@Schema(description = "PartyId", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = REQUIRED)
	private String partyId;

	@Schema(description = "Asset id", example = "PRH-123456789")
	private String assetId;

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

	public static AssetSearchRequest create() {
		return new AssetSearchRequest();
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public AssetSearchRequest withPartyId(String partyId) {
		this.partyId = partyId;
		return this;
	}

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	public AssetSearchRequest withAssetId(String assetId) {
		this.assetId = assetId;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public AssetSearchRequest withType(String type) {
		this.type = type;
		return this;
	}

	public LocalDate getIssued() {
		return issued;
	}

	public void setIssued(LocalDate issued) {
		this.issued = issued;
	}

	public AssetSearchRequest withIssued(LocalDate issued) {
		this.issued = issued;
		return this;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	public AssetSearchRequest withValidTo(LocalDate validTo) {
		this.validTo = validTo;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public AssetSearchRequest withStatus(Status status) {
		this.status = status;
		return this;
	}

	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}

	public AssetSearchRequest withStatusReason(String statusReason) {
		this.statusReason = statusReason;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AssetSearchRequest withDescription(String description) {
		this.description = description;
		return this;
	}

	public Map<String, String> getAdditionalParameters() {
		return additionalParameters;
	}

	public void setAdditionalParameters(Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
	}

	public AssetSearchRequest withAdditionalParameters(Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(additionalParameters, assetId, description, issued, partyId, status, statusReason, type, validTo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AssetSearchRequest)) {
			return false;
		}
		AssetSearchRequest other = (AssetSearchRequest) obj;
		return Objects.equals(additionalParameters, other.additionalParameters) && Objects.equals(assetId, other.assetId) && Objects.equals(description, other.description) && Objects.equals(issued, other.issued) && Objects.equals(partyId,
			other.partyId) && status == other.status && Objects.equals(statusReason, other.statusReason) && Objects.equals(type, other.type) && Objects.equals(validTo, other.validTo);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AssetSearchRequest [partyId=").append(partyId).append(", assetId=").append(assetId).append(", type=").append(type).append(", issued=").append(issued).append(", validTo=").append(validTo).append(", status=").append(status)
			.append(", statusReason=").append(statusReason).append(", description=").append(description).append(", additionalParameters=").append(additionalParameters).append("]");
		return builder.toString();
	}
}
