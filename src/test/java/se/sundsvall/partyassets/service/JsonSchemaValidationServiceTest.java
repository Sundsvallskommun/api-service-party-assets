package se.sundsvall.partyassets.service;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.partyassets.integration.jsonschema.JsonSchemaClient;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JsonSchemaValidationServiceTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Mock
	private JsonSchemaClient jsonSchemaClientMock;

	@InjectMocks
	private JsonSchemaValidationService jsonSchemaValidationService;

	@Test
	void validateSuccessfully() {
		// Arrange
		final var municipalityId = "2281";
		final var schemaId = "2281_schema_1.0";
		final var jsonValue = OBJECT_MAPPER.createObjectNode().put("productId", 1);

		doNothing().when(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonValue);

		// Act & Assert
		assertDoesNotThrow(() -> jsonSchemaValidationService.validate(municipalityId, schemaId, jsonValue));

		verify(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonValue);
	}

	@Test
	void validateWithValidationFailure() {
		// Arrange
		final var municipalityId = "2281";
		final var schemaId = "2281_schema_1.0";
		final var jsonValue = OBJECT_MAPPER.createObjectNode().put("invalid", true);
		final var request = Request.create(Request.HttpMethod.POST, "url", java.util.Map.of(), null, new RequestTemplate());
		final var validationError = new FeignException.BadRequest("json-schema error", request, "Validation failed: required property 'productId' not found".getBytes(), null);

		doThrow(validationError).when(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonValue);

		// Act
		final var exception = assertThrows(FeignException.class,
			() -> jsonSchemaValidationService.validate(municipalityId, schemaId, jsonValue));

		// Assert
		assertThat(exception).isSameAs(validationError);

		verify(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonValue);
	}

	@Test
	void validateWithSchemaNotFound() {
		// Arrange
		final var municipalityId = "2281";
		final var schemaId = "2281_nonexistent_1.0";
		final var jsonValue = OBJECT_MAPPER.createObjectNode().put("productId", 1);
		final var request = Request.create(Request.HttpMethod.POST, "url", java.util.Map.of(), null, new RequestTemplate());
		final var notFoundError = new FeignException.NotFound("json-schema error", request, "Schema not found: 2281_nonexistent_1.0".getBytes(), null);

		doThrow(notFoundError).when(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonValue);

		// Act
		final var exception = assertThrows(FeignException.class,
			() -> jsonSchemaValidationService.validate(municipalityId, schemaId, jsonValue));

		// Assert
		assertThat(exception).isSameAs(notFoundError);

		verify(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonValue);
	}

	@Test
	void validateWithServerError() {
		// Arrange
		final var municipalityId = "2281";
		final var schemaId = "2281_schema_1.0";
		final var jsonValue = OBJECT_MAPPER.createObjectNode().put("productId", 1);
		final var request = Request.create(Request.HttpMethod.POST, "url", java.util.Map.of(), null, new RequestTemplate());
		final var serverError = new FeignException.InternalServerError("json-schema error", request, "Internal Server Error".getBytes(), null);

		doThrow(serverError).when(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonValue);

		// Act
		final var exception = assertThrows(FeignException.class,
			() -> jsonSchemaValidationService.validate(municipalityId, schemaId, jsonValue));

		// Assert
		assertThat(exception).isSameAs(serverError);

		verify(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonValue);
	}
}
