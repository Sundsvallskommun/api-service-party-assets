package se.sundsvall.partyassets.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.Objects;

public class JsonSchema {

	@Schema(description = "Schema ID. The ID is composed by the schema name and version. I.e.: [schema name]_[schema_version]", example = "person_1.0")
	private String id;

	@Schema(description = "Schema name", example = "person")
	private String name;

	@Schema(description = "Schema version on the format [major version].[minor version]", example = "1.0")
	private String version;

	@Schema(description = "The number of schema references. I.e. number of json-objects that references the schema.", example = "42")
	private long numberOfReferences;

	@Schema(description = "The JSON schema", example = """
		{
		  "$id": "https://example.com/person.schema.json",
		  "$schema": "https://json-schema.org/draft/2020-12/schema",
		  "title": "Person",
		  "type": "object",
		  "properties": {
		    "firstName": {
		      "type": "string",
		      "description": "The person's first name."
		    },
		    "lastName": {
		      "type": "string",
		      "description": "The person's last name."
		    }
		  }
		}
		""")
	private String value;

	@Schema(description = "Description of the schema purpose", example = "A JSON-schema that defines a person object")
	private String description;

	@Schema(description = "Created timestamp")
	private OffsetDateTime created;

	public static JsonSchema create() {
		return new JsonSchema();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public JsonSchema withId(String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JsonSchema withName(String name) {
		this.name = name;
		return this;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public JsonSchema withVersion(String version) {
		this.version = version;
		return this;
	}

	public long getNumberOfReferences() {
		return numberOfReferences;
	}

	public void setNumberOfReferences(long numberOfReferences) {
		this.numberOfReferences = numberOfReferences;
	}

	public JsonSchema withNumberOfReferences(long numberOfReferences) {
		this.numberOfReferences = numberOfReferences;
		return this;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public JsonSchema withValue(String value) {
		this.value = value;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public JsonSchema withDescription(String description) {
		this.description = description;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	public JsonSchema withCreated(OffsetDateTime created) {
		this.created = created;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(created, description, id, name, numberOfReferences, value, version);
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
		JsonSchema other = (JsonSchema) obj;
		return Objects.equals(created, other.created) && Objects.equals(description, other.description) && Objects.equals(id, other.id) && Objects.equals(name, other.name) && numberOfReferences == other.numberOfReferences && Objects.equals(value,
			other.value) && Objects.equals(version, other.version);
	}

	@Override
	public String toString() {
		return "JsonSchema [id=" + id + ", name=" + name + ", version=" + version + ", numberOfReferences=" + numberOfReferences + ", value=" + value + ", description=" + description + ", created=" + created + "]";
	}
}
