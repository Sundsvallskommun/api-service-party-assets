package se.sundsvall.partyassets.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Blob;
import java.util.Objects;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "asset_attachment_data")
public class AssetAttachmentDataEntity {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "file", columnDefinition = "longblob")
	private Blob file;

	public static AssetAttachmentDataEntity create() {
		return new AssetAttachmentDataEntity();
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public AssetAttachmentDataEntity withId(final Long id) {
		this.id = id;
		return this;
	}

	public Blob getFile() {
		return file;
	}

	public void setFile(final Blob file) {
		this.file = file;
	}

	public AssetAttachmentDataEntity withFile(final Blob file) {
		this.file = file;
		return this;
	}

	// The blob is left out of identity: java.sql.Blob has no value semantics, so comparing it falls back to reference
	// equality and two rows holding the same bytes would still differ.
	@Override
	public int hashCode() {
		return Objects.hash(id);
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
		final AssetAttachmentDataEntity other = (AssetAttachmentDataEntity) obj;
		return id != null && Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "AssetAttachmentDataEntity [id=" + id + "]";
	}
}
