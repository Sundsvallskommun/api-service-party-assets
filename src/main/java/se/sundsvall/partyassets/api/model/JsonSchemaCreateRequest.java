package se.sundsvall.partyassets.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;
import se.sundsvall.partyassets.api.validation.ValidJsonSchema;

public class JsonSchemaCreateRequest {

	@NotBlank
	@Schema(description = "Schema name", example = "person", requiredMode = REQUIRED)
	private String name;

	@NotNull
	@Pattern(regexp = "^(\\d+\\.)?(\\d+)$")
	@Schema(description = "Schema version on the format [major version].[minor version]", example = "1.0", requiredMode = REQUIRED)
	private String version;

	@ValidJsonSchema
	@Schema(description = "The JSON schema, specified by: https://json-schema.org/draft/2020-12/schema", example = """
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
		""", requiredMode = REQUIRED)
	private String value;

	@Schema(description = "Description of the schema purpose", example = "A JSON-schema that defines a person object")
	private String description;

	public static JsonSchemaCreateRequest create() {
		return new JsonSchemaCreateRequest();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JsonSchemaCreateRequest withName(String name) {
		this.name = name;
		return this;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public JsonSchemaCreateRequest withVersion(String version) {
		this.version = version;
		return this;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public JsonSchemaCreateRequest withValue(String value) {
		this.value = value;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public JsonSchemaCreateRequest withDescription(String description) {
		this.description = description;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(description, name, value, version);
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
		JsonSchemaCreateRequest other = (JsonSchemaCreateRequest) obj;
		return Objects.equals(description, other.description) && Objects.equals(name, other.name) && Objects.equals(value, other.value) && Objects.equals(version, other.version);
	}

	@Override
	public String toString() {
		return "JsonSchemaCreateRequest [name=" + name + ", version=" + version + ", value=" + value + ", description=" + description + "]";
	}
}
