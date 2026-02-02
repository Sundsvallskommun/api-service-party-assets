package se.sundsvall.partyassets.service;

import static org.zalando.problem.Status.BAD_REQUEST;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.partyassets.integration.jsonschema.JsonSchemaClient;

@Service
public class JsonSchemaValidationService {

	private final JsonSchemaClient jsonSchemaClient;
	private final ObjectMapper objectMapper;

	public JsonSchemaValidationService(JsonSchemaClient jsonSchemaClient, ObjectMapper objectMapper) {
		this.jsonSchemaClient = jsonSchemaClient;
		this.objectMapper = objectMapper;
	}

	/**
	 * Validates JSON value against schema using external service.
	 *
	 * @param municipalityId municipality ID (from path variable)
	 * @param schemaId       schema ID
	 * @param jsonValue      JSON value to validate
	 */
	public void validate(String municipalityId, String schemaId, String jsonValue) {
		try {
			final var jsonNode = objectMapper.readTree(jsonValue);
			jsonSchemaClient.validateJson(municipalityId, schemaId, jsonNode);
		} catch (JsonProcessingException e) {
			throw Problem.valueOf(BAD_REQUEST, "Invalid JSON: " + e.getMessage());
		}
	}
}
