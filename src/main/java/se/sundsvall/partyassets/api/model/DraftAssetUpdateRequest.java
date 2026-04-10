package se.sundsvall.partyassets.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Objects;
import se.sundsvall.partyassets.api.validation.ValidStatusReason;

@ValidStatusReason
public class DraftAssetUpdateRequest extends AssetUpdateRequest {

	@Schema(description = "Valid to date", examples = "2021-12-31")
	private LocalDate validTo;

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
		DraftAssetUpdateRequest other = (DraftAssetUpdateRequest) obj;
		return Objects.equals(getAdditionalParameters(), other.getAdditionalParameters()) && Objects.equals(getJsonParameters(), other.getJsonParameters()) && getStatus() == other.getStatus() && Objects.equals(getStatusReason(),
			other.getStatusReason()) && Objects.equals(validTo, other.validTo);
	}

	@Override
	public String toString() {
		return "DraftAssetUpdateRequest [validTo=" + validTo + ", status=" + getStatus() + ", statusReason=" + getStatusReason() + ", additionalParameters=" + getAdditionalParameters() + ", jsonParameters=" + getJsonParameters() + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(getAdditionalParameters(), getJsonParameters(), getStatus(), getStatusReason(), validTo);
	}
}
