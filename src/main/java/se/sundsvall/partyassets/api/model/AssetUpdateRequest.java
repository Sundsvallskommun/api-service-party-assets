package se.sundsvall.partyassets.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import se.sundsvall.partyassets.api.validation.ValidJsonParameter;
import se.sundsvall.partyassets.api.validation.ValidStatusReason;

@ValidStatusReason
public class AssetUpdateRequest {
	@Schema(description = "Asset status", examples = "ACTIVE")
	private Status status;

	@Schema(description = "Status reason", examples = "Status reason")
	private String statusReason;

	@Schema(description = "Valid to date. Can only be changed while the asset is ACTIVE or TEMPORARY", examples = "2021-12-31")
	private LocalDate validTo;

	@Schema(description = "If true, validTo will be cleared (asset becomes indefinite). Takes precedence over validTo when both are supplied. Can only be used while the asset is ACTIVE or TEMPORARY", examples = "true")
	private Boolean indefinitely;

	@Schema(description = "Asset title, shown to the party. Can only be changed while the asset is ACTIVE or TEMPORARY", examples = "Stadigvarande tillstånd för servering av alkohol")
	@Size(max = 255)
	private String title;

	@Schema(description = "Additional parameters, replacing the current ones. Can only be changed while the asset is ACTIVE or TEMPORARY", examples = "{\"foo\":\"bar\"}")
	private Map<String, String> additionalParameters;

	@Schema(description = "JSON parameters, replacing the current ones. Can only be changed while the asset is ACTIVE or TEMPORARY")
	private List<@ValidJsonParameter AssetJsonParameter> jsonParameters;

	public static AssetUpdateRequest create() {
		return new AssetUpdateRequest();
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

	public Boolean getIndefinitely() {
		return indefinitely;
	}

	public void setIndefinitely(Boolean indefinitely) {
		this.indefinitely = indefinitely;
	}

	public AssetUpdateRequest withIndefinitely(Boolean indefinitely) {
		this.indefinitely = indefinitely;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public AssetUpdateRequest withTitle(String title) {
		this.title = title;
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

	public List<AssetJsonParameter> getJsonParameters() {
		return jsonParameters;
	}

	public void setJsonParameters(List<AssetJsonParameter> jsonParameters) {
		this.jsonParameters = jsonParameters;
	}

	public AssetUpdateRequest withJsonParameters(List<AssetJsonParameter> jsonParameters) {
		this.jsonParameters = jsonParameters;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(additionalParameters, indefinitely, jsonParameters, status, statusReason, title, validTo);
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
		AssetUpdateRequest other = (AssetUpdateRequest) obj;
		return Objects.equals(additionalParameters, other.additionalParameters) && Objects.equals(indefinitely, other.indefinitely) && Objects.equals(jsonParameters, other.jsonParameters) && status == other.status
			&& Objects.equals(statusReason, other.statusReason) && Objects.equals(title, other.title) && Objects.equals(validTo, other.validTo);
	}

	@Override
	public String toString() {
		return "AssetUpdateRequest [status=" + status + ", statusReason=" + statusReason + ", validTo=" + validTo + ", indefinitely=" + indefinitely + ", title=" + title + ", additionalParameters=" + additionalParameters + ", jsonParameters="
			+ jsonParameters + "]";
	}
}
