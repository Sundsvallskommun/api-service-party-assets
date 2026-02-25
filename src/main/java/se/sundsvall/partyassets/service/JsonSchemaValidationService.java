package se.sundsvall.partyassets.service;

import org.springframework.stereotype.Service;
import se.sundsvall.partyassets.integration.jsonschema.JsonSchemaClient;
import tools.jackson.databind.JsonNode;

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
