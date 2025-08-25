package se.sundsvall.partyassets.integration.db.model;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.hibernate.Length.LONG32;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.hibernate.annotations.TimeZoneStorage;

@Entity
@Table(name = "json_schema",
	uniqueConstraints = {
		@UniqueConstraint(name = "uc_json_schema_municipality_id_name_version", columnNames = {
			"municipality_id",
			"name",
			"version"
		})
	})
public class JsonSchemaEntity {

	@Id
	private String id;

	@Column(name = "municipality_id", length = 8)
	private String municipalityId;

	@Column(name = "name", length = 64)
	private String name;

	@Column(name = "version", length = 32)
	private String version;

	@Column(name = "value", length = LONG32)
	private String value;

	@Column(name = "description", length = LONG32)
	private String description;

	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	public static JsonSchemaEntity create() {
		return new JsonSchemaEntity();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public JsonSchemaEntity withId(String id) {
		this.id = id;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public JsonSchemaEntity withMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JsonSchemaEntity withName(String name) {
		this.name = name;
		return this;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public JsonSchemaEntity withVersion(String version) {
		this.version = version;
		return this;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public JsonSchemaEntity withValue(String value) {
		this.value = value;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public JsonSchemaEntity withDescription(String description) {
		this.description = description;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	public JsonSchemaEntity withCreated(OffsetDateTime created) {
		this.created = created;
		return this;
	}

	@PrePersist
	void prePersist() {
		created = now(systemDefault()).truncatedTo(MILLIS);
	}

	@Override
	public int hashCode() {
		return Objects.hash(created, description, id, municipalityId, name, value, version);
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
		JsonSchemaEntity other = (JsonSchemaEntity) obj;
		return Objects.equals(created, other.created) && Objects.equals(description, other.description) && Objects.equals(id, other.id) && Objects.equals(municipalityId, other.municipalityId) && Objects.equals(name, other.name) && Objects.equals(value,
			other.value) && Objects.equals(version, other.version);
	}

	@Override
	public String toString() {
		return "JsonSchemaEntity [id=" + id + ", municipalityId=" + municipalityId + ", name=" + name + ", version=" + version + ", value=" + value + ", description=" + description + ", created=" + created + "]";
	}
}
