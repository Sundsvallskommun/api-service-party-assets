package se.sundsvall.partyassets.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

public class AssetUpdateRequest {
	@Schema(description = "Asset status", examples = "ACTIVE")
	private Status status;

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

	@Override
	public int hashCode() {
		return Objects.hash(status);
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
		return status == other.status;
	}

	@Override
	public String toString() {
		return "AssetUpdateRequest [status=" + status + "]";
	}
}
