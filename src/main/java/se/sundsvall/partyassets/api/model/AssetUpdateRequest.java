package se.sundsvall.partyassets.api.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import se.sundsvall.partyassets.api.validation.ValidStatusReason;

@ValidStatusReason
public class AssetUpdateRequest {

	@Schema(description = "Case reference ids", example = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
	private List<String> caseReferenceIds;

	@Schema(description = "Valid to date", example = "2021-12-31")
	private LocalDate validTo;

	@Schema(description = "Asset status", example = "ACTIVE")
	private Status status;

	@Schema(description = "Status reason", example = "Status reason")
	private String statusReason;

	@Schema(description = "Additional parameters", example = "{\"foo\":\"bar\"}")
	private Map<String, String> additionalParameters;

	public static AssetUpdateRequest create() {
		return new AssetUpdateRequest();
	}

	public List<String> getCaseReferenceIds() {
		return caseReferenceIds;
	}

	public void setCaseReferenceIds(List<String> caseReferenceIds) {
		this.caseReferenceIds = caseReferenceIds;
	}

	public AssetUpdateRequest withCaseReferenceIds(List<String> caseReferenceIds) {
		this.caseReferenceIds = caseReferenceIds;
		return this;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	public AssetUpdateRequest withValidTo(LocalDate validTo) {
		this.validTo = validTo;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public AssetUpdateRequest withStatus(Status status) {
		this.status = status;
		return this;
	}

	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}

	public AssetUpdateRequest withStatusReason(String statusReason) {
		this.statusReason = statusReason;
		return this;
	}

	public Map<String, String> getAdditionalParameters() {
		return additionalParameters;
	}

	public void setAdditionalParameters(Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
	}

	public AssetUpdateRequest withAdditionalParameters(Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(additionalParameters, caseReferenceIds, status, statusReason, validTo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AssetUpdateRequest)) {
			return false;
		}
		AssetUpdateRequest other = (AssetUpdateRequest) obj;
		return Objects.equals(additionalParameters, other.additionalParameters) && Objects.equals(caseReferenceIds, other.caseReferenceIds) && status == other.status && Objects.equals(statusReason, other.statusReason) && Objects.equals(validTo,
			other.validTo);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AssetUpdateRequest [caseReferenceIds=").append(caseReferenceIds).append(", validTo=").append(validTo).append(", status=").append(status).append(", statusReason=").append(statusReason).append(", additionalParameters=").append(
			additionalParameters).append("]");
		return builder.toString();
	}
}
