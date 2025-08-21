package se.sundsvall.partyassets.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

public class JsonParameter {

	@NotBlank
	@Schema(description = "Parameter key", example = "personParameter")
	private String key;

	@Schema(description = "Parameter value with the JSON structure", example = """
		{
		  "firstName": "Joe",
		  "lastName": "Doe"
		}
		""")
	private String value;

	@NotBlank
	@Schema(description = "Schema ID", example = "person_1.0")
	private String schemaId;

	public static JsonParameter create() {
		return new JsonParameter();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public JsonParameter withKey(String key) {
		this.key = key;
		return this;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public JsonParameter withValue(String value) {
		this.value = value;
		return this;
	}

	public String getSchemaId() {
		return schemaId;
	}

	public void setSchemaId(String schemaId) {
		this.schemaId = schemaId;
	}

	public JsonParameter withSchemaId(String schemaId) {
		this.schemaId = schemaId;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, schemaId, value);
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
		JsonParameter other = (JsonParameter) obj;
		return Objects.equals(key, other.key) && Objects.equals(schemaId, other.schemaId) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "JsonParameter [key=" + key + ", value=" + value + ", schemaId=" + schemaId + "]";
	}
}
