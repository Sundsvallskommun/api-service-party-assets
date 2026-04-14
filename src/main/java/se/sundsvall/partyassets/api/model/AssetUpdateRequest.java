package se.sundsvall.partyassets.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import se.sundsvall.partyassets.api.validation.ValidStatusReason;

@ValidStatusReason
public class AssetUpdateRequest {
	@Schema(description = "Asset status", examples = "ACTIVE")
	private Status status;

	@Schema(description = "Status reason", examples = "Status reason")
	private String statusReason;

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

	@Override
	public int hashCode() {
		return Objects.hash(status, statusReason);
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
		return status == other.status && Objects.equals(statusReason, other.statusReason);
	}

	@Override
	public String toString() {
		return "AssetUpdateRequest [status=" + status + ", statusReason=" + statusReason + "]";
	}
}
