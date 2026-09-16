package se.sundsvall.partyassets.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.UuidGenerator;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Objects.nonNull;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

@Entity
@Table(name = "asset_attachment",
	indexes = {
		@Index(name = "idx_asset_attachment_asset_id_municipality_id", columnList = "asset_id, municipality_id")
	},
	uniqueConstraints = {
		@UniqueConstraint(name = "uc_asset_attachment_data_id", columnNames = {
			"asset_attachment_data_id"
		})
	})
public class AssetAttachmentEntity {

	@Id
	@UuidGenerator
	private String id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "asset_id", nullable = false, foreignKey = @ForeignKey(name = "fk_asset_attachment_asset_id"))
	private AssetEntity asset;

	@OneToOne(fetch = LAZY, cascade = ALL, orphanRemoval = true)
	@JoinColumn(name = "asset_attachment_data_id", nullable = false, foreignKey = @ForeignKey(name = "fk_asset_attachment_data_id"))
	private AssetAttachmentDataEntity attachmentData;

	@Column(name = "municipality_id", length = 8, nullable = false)
	private String municipalityId;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "mime_type")
	private String mimeType;

	@Column(name = "file_size")
	private Integer fileSize;

	@Column(name = "category")
	private String category;

	@Column(name = "description")
	private String description;

	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

	public static AssetAttachmentEntity create() {
		return new AssetAttachmentEntity();
	}

	// Adding an attachment counts as changing the permit itself, so the asset gets a new updated timestamp - the same
	// rule AssetJsonParameterEntity follows. A copy being built for a draft has no created yet and is left alone.
	@PrePersist
	void prePersist() {
		created = now(systemDefault()).truncatedTo(MILLIS);

		if (nonNull(asset) && nonNull(asset.getCreated())) {
			asset.preUpdate();
		}
	}

	@PreUpdate
	void preUpdate() {
		updated = now(systemDefault()).truncatedTo(MILLIS);
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public AssetAttachmentEntity withId(final String id) {
		this.id = id;
		return this;
	}

	public AssetEntity getAsset() {
		return asset;
	}

	public void setAsset(final AssetEntity asset) {
		this.asset = asset;
	}

	public AssetAttachmentEntity withAsset(final AssetEntity asset) {
		this.asset = asset;
		return this;
	}

	public AssetAttachmentDataEntity getAttachmentData() {
		return attachmentData;
	}

	public void setAttachmentData(final AssetAttachmentDataEntity attachmentData) {
		this.attachmentData = attachmentData;
	}

	public AssetAttachmentEntity withAttachmentData(final AssetAttachmentDataEntity attachmentData) {
		this.attachmentData = attachmentData;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public AssetAttachmentEntity withMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public AssetAttachmentEntity withFileName(final String fileName) {
		this.fileName = fileName;
		return this;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(final String mimeType) {
		this.mimeType = mimeType;
	}

	public AssetAttachmentEntity withMimeType(final String mimeType) {
		this.mimeType = mimeType;
		return this;
	}

	public Integer getFileSize() {
		return fileSize;
	}

	public void setFileSize(final Integer fileSize) {
		this.fileSize = fileSize;
	}

	public AssetAttachmentEntity withFileSize(final Integer fileSize) {
		this.fileSize = fileSize;
		return this;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	public AssetAttachmentEntity withCategory(final String category) {
		this.category = category;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public AssetAttachmentEntity withDescription(final String description) {
		this.description = description;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(final OffsetDateTime created) {
		this.created = created;
	}

	public AssetAttachmentEntity withCreated(final OffsetDateTime created) {
		this.created = created;
		return this;
	}

	public OffsetDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(final OffsetDateTime updated) {
		this.updated = updated;
	}

	public AssetAttachmentEntity withUpdated(final OffsetDateTime updated) {
		this.updated = updated;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(category, created, description, fileName, fileSize, id, mimeType, municipalityId, updated);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AssetAttachmentEntity other = (AssetAttachmentEntity) obj;
		return Objects.equals(category, other.category) && Objects.equals(created, other.created) && Objects.equals(description, other.description) && Objects.equals(fileName, other.fileName) && Objects.equals(fileSize, other.fileSize) && Objects.equals(
			id, other.id) && Objects.equals(mimeType, other.mimeType) && Objects.equals(municipalityId, other.municipalityId) && Objects.equals(updated, other.updated);
	}

	// The asset is left out: AssetEntity uses field access, so even reading its id off the proxy would initialize it.
	@Override
	public String toString() {
		return "AssetAttachmentEntity [id=" + id + ", municipalityId=" + municipalityId + ", fileName=" + fileName + ", mimeType=" + mimeType + ", fileSize=" + fileSize + ", category=" + category + ", description=" + description + ", created=" + created
			+ ", updated=" + updated + "]";
	}
}
