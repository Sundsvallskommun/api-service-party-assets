package se.sundsvall.partyassets.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.exception.ServerProblem;
import se.sundsvall.partyassets.integration.jsonschema.JsonSchemaClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.zalando.problem.Status.BAD_GATEWAY;

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
		final var validationError = new ClientProblem(BAD_GATEWAY, "json-schema error: {detail=Validation failed: required property 'productId' not found, status=400 Bad Request, title=Bad Request}");

		doThrow(validationError).when(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonValue);

		// Act
		final var exception = assertThrows(ClientProblem.class,
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
		final var notFoundError = new ClientProblem(BAD_GATEWAY, "json-schema error: {detail=Schema not found: 2281_nonexistent_1.0, status=404 Not Found, title=Not Found}");

		doThrow(notFoundError).when(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonValue);

		// Act
		final var exception = assertThrows(ClientProblem.class,
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
		final var serverError = new ServerProblem(BAD_GATEWAY, "json-schema error: {detail=Internal Server Error, status=500 Internal Server Error, title=Internal Server Error}");

		doThrow(serverError).when(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonValue);

		// Act
		final var exception = assertThrows(ServerProblem.class,
			() -> jsonSchemaValidationService.validate(municipalityId, schemaId, jsonValue));

		// Assert
		assertThat(exception).isSameAs(serverError);

		verify(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonValue);
	}
}
