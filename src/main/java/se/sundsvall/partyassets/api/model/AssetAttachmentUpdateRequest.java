package se.sundsvall.partyassets.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.Objects;

@Schema(description = "Attachment update request. Null values are left untouched")
public class AssetAttachmentUpdateRequest {

	@Size(max = 255)
	@Schema(description = "Name of the file", examples = "lokalritning.pdf")
	private String fileName;

	@Size(max = 255)
	@Schema(description = "What the attachment depicts", examples = "LOKALRITNING")
	private String category;

	@Size(max = 255)
	@Schema(description = "Attachment description", examples = "Ritning over serveringslokal, plan 2")
	private String description;

	public static AssetAttachmentUpdateRequest create() {
		return new AssetAttachmentUpdateRequest();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public AssetAttachmentUpdateRequest withFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public AssetAttachmentUpdateRequest withCategory(String category) {
		this.category = category;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AssetAttachmentUpdateRequest withDescription(String description) {
		this.description = description;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(category, description, fileName);
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
		AssetAttachmentUpdateRequest other = (AssetAttachmentUpdateRequest) obj;
		return Objects.equals(category, other.category) && Objects.equals(description, other.description) && Objects.equals(fileName, other.fileName);
	}

	@Override
	public String toString() {
		return "AssetAttachmentUpdateRequest [fileName=" + fileName + ", category=" + category + ", description=" + description + "]";
	}
}
