package se.sundsvall.partyassets.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import se.sundsvall.partyassets.integration.jsonschema.JsonSchemaClient;

@Service
public class JsonSchemaValidationService {

	private final JsonSchemaClient jsonSchemaClient;

	public JsonSchemaValidationService(JsonSchemaClient jsonSchemaClient) {
		this.jsonSchemaClient = jsonSchemaClient;
	}

	public void validate(String municipalityId, String schemaId, JsonNode jsonValue) {
		jsonSchemaClient.validateJson(municipalityId, schemaId, jsonValue);
	}
}
