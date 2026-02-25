package se.sundsvall.partyassets.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import tools.jackson.databind.JsonNode;

public class AssetJsonParameter {

	@NotBlank
	@Schema(description = "Parameter key", examples = "personParameter")
	private String key;

	@NotNull
	@Schema(description = "Parameter value with the JSON structure", example = """
		{
		  "firstName": "Joe",
		  "lastName": "Doe"
		}
		""")
	private JsonNode value;

	@NotBlank
	@Schema(description = "Schema ID", examples = "person_1.0")
	private String schemaId;

	public static AssetJsonParameter create() {
		return new AssetJsonParameter();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public AssetJsonParameter withKey(String key) {
		this.key = key;
		return this;
	}

	public JsonNode getValue() {
		return value;
	}

	public void setValue(JsonNode value) {
		this.value = value;
	}

	public AssetJsonParameter withValue(JsonNode value) {
		this.value = value;
		return this;
	}

	public String getSchemaId() {
		return schemaId;
	}

	public void setSchemaId(String schemaId) {
		this.schemaId = schemaId;
	}

	public AssetJsonParameter withSchemaId(String schemaId) {
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
		AssetJsonParameter other = (AssetJsonParameter) obj;
		return Objects.equals(key, other.key) && Objects.equals(schemaId, other.schemaId) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "AssetJsonParameter [key=" + key + ", value=" + value + ", schemaId=" + schemaId + "]";
	}
}
