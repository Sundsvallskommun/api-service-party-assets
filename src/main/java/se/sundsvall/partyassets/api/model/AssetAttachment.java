package se.sundsvall.partyassets.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.Objects;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Schema(description = "Metadata for a file attached to an asset")
public class AssetAttachment {

	@Schema(description = "Unique id of attachment", examples = "1c8f38a6-b492-4037-b7dc-de5bc6c629f0", accessMode = READ_ONLY)
	private String id;

	@Schema(description = "Name of the file", examples = "lokalritning.pdf")
	private String fileName;

	@Schema(description = "Mime type of the file", examples = "application/pdf", accessMode = READ_ONLY)
	private String mimeType;

	@Schema(description = "Size of the file in bytes", examples = "1024", accessMode = READ_ONLY)
	private Integer fileSize;

	@Schema(description = "What the attachment depicts", examples = "LOKALRITNING")
	private String category;

	@Schema(description = "Attachment description", examples = "Ritning over serveringslokal, plan 2")
	private String description;

	@Schema(description = "Timestamp when the attachment was created", examples = "2023-01-01T12:00:00+01:00", accessMode = READ_ONLY)
	private OffsetDateTime created;

	@Schema(description = "Timestamp when the attachment was last updated", examples = "2023-01-02T12:00:00+01:00", accessMode = READ_ONLY)
	private OffsetDateTime updated;

	public static AssetAttachment create() {
		return new AssetAttachment();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public AssetAttachment withId(String id) {
		this.id = id;
		return this;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public AssetAttachment withFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public AssetAttachment withMimeType(String mimeType) {
		this.mimeType = mimeType;
		return this;
	}

	public Integer getFileSize() {
		return fileSize;
	}

	public void setFileSize(Integer fileSize) {
		this.fileSize = fileSize;
	}

	public AssetAttachment withFileSize(Integer fileSize) {
		this.fileSize = fileSize;
		return this;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public AssetAttachment withCategory(String category) {
		this.category = category;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AssetAttachment withDescription(String description) {
		this.description = description;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	public AssetAttachment withCreated(OffsetDateTime created) {
		this.created = created;
		return this;
	}

	public OffsetDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(OffsetDateTime updated) {
		this.updated = updated;
	}

	public AssetAttachment withUpdated(OffsetDateTime updated) {
		this.updated = updated;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(category, created, description, fileName, fileSize, id, mimeType, updated);
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
		AssetAttachment other = (AssetAttachment) obj;
		return Objects.equals(category, other.category) && Objects.equals(created, other.created) && Objects.equals(description, other.description) && Objects.equals(fileName, other.fileName) && Objects.equals(fileSize, other.fileSize) && Objects.equals(
			id, other.id) && Objects.equals(mimeType, other.mimeType) && Objects.equals(updated, other.updated);
	}

	@Override
	public String toString() {
		return "AssetAttachment [id=" + id + ", fileName=" + fileName + ", mimeType=" + mimeType + ", fileSize=" + fileSize + ", category=" + category + ", description=" + description + ", created=" + created + ", updated=" + updated + "]";
	}
}
