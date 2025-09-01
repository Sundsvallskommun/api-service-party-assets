package se.sundsvall.partyassets.integration.db.model;

import static jakarta.persistence.FetchType.LAZY;
import static java.util.Objects.nonNull;
import static org.hibernate.Length.LONG32;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.Objects;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "asset_json_parameter")
public class AssetJsonParameterEntity {

	@Id
	@UuidGenerator
	private String id;

	@Column(name = "parameter_key")
	private String key;

	@Column(name = "parameter_value", length = LONG32)
	private String value;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "asset_id", nullable = false, foreignKey = @ForeignKey(name = "fk_asset_json_parameter_asset_id"))
	private AssetEntity asset;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "schema_id", nullable = false, foreignKey = @ForeignKey(name = "fk_asset_json_parameter_schema_id"))
	private JsonSchemaEntity schema;

	public static AssetJsonParameterEntity create() {
		return new AssetJsonParameterEntity();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public AssetJsonParameterEntity withId(String id) {
		this.id = id;
		return this;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public AssetJsonParameterEntity withKey(String key) {
		this.key = key;
		return this;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public AssetJsonParameterEntity withValue(String value) {
		this.value = value;
		return this;
	}

	public AssetEntity getAsset() {
		return asset;
	}

	public void setAsset(AssetEntity asset) {
		this.asset = asset;
	}

	public AssetJsonParameterEntity withAsset(AssetEntity asset) {
		this.asset = asset;
		return this;
	}

	public JsonSchemaEntity getSchema() {
		return schema;
	}

	public void setSchema(JsonSchemaEntity schema) {
		this.schema = schema;
	}

	public AssetJsonParameterEntity withSchema(JsonSchemaEntity schema) {
		this.schema = schema;
		return this;
	}

	@PrePersist
	void prePersist() {
		if (nonNull(this.asset.getCreated())) {
			this.asset.preUpdate();
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(asset, id, key, schema, value);
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
		AssetJsonParameterEntity other = (AssetJsonParameterEntity) obj;
		return Objects.equals((asset != null ? asset.getId() : 0), (other.asset != null ? other.asset.getId() : 0)) && Objects.equals(id, other.id) && Objects.equals(key, other.key) && Objects.equals(schema, other.schema) && Objects.equals(value,
			other.value);
	}

	@Override
	public String toString() {
		return "AssetJsonParameterEntity [id=" + id + ", key=" + key + ", value=" + value + ", asset=" + (asset != null ? asset.getId() : 0) + ", schema=" + schema + "]";
	}
}
