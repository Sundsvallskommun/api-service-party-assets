package se.sundsvall.partyassets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.BAD_GATEWAY;
import static org.zalando.problem.Status.BAD_REQUEST;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.exception.ServerProblem;
import se.sundsvall.partyassets.integration.jsonschema.JsonSchemaClient;

@ExtendWith(MockitoExtension.class)
class JsonSchemaValidationServiceTest {

	@Mock
	private JsonSchemaClient jsonSchemaClientMock;

	@Mock
	private ObjectMapper objectMapperMock;

	@Mock
	private JsonNode jsonNodeMock;

	@InjectMocks
	private JsonSchemaValidationService jsonSchemaValidationService;

	@Test
	void validateSuccessfully() throws JsonProcessingException {
		// Arrange
		final var municipalityId = "2281";
		final var schemaId = "2281_schema_1.0";
		final var jsonValue = "{\"productId\": 1}";

		when(objectMapperMock.readTree(jsonValue)).thenReturn(jsonNodeMock);
		doNothing().when(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonNodeMock);

		// Act & Assert
		assertDoesNotThrow(() -> jsonSchemaValidationService.validate(municipalityId, schemaId, jsonValue));

		verify(objectMapperMock).readTree(jsonValue);
		verify(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonNodeMock);
	}

	@Test
	void validateWithValidationFailure() throws JsonProcessingException {
		// Arrange
		final var municipalityId = "2281";
		final var schemaId = "2281_schema_1.0";
		final var jsonValue = "{\"invalid\": true}";
		final var validationError = new ClientProblem(BAD_GATEWAY, "json-schema error: {detail=Validation failed: required property 'productId' not found, status=400 Bad Request, title=Bad Request}");

		when(objectMapperMock.readTree(jsonValue)).thenReturn(jsonNodeMock);
		doThrow(validationError).when(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonNodeMock);

		// Act
		final var exception = assertThrows(ClientProblem.class,
			() -> jsonSchemaValidationService.validate(municipalityId, schemaId, jsonValue));

		// Assert
		assertThat(exception).isSameAs(validationError);

		verify(objectMapperMock).readTree(jsonValue);
		verify(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonNodeMock);
	}

	@Test
	void validateWithSchemaNotFound() throws JsonProcessingException {
		// Arrange
		final var municipalityId = "2281";
		final var schemaId = "2281_nonexistent_1.0";
		final var jsonValue = "{\"productId\": 1}";
		final var notFoundError = new ClientProblem(BAD_GATEWAY, "json-schema error: {detail=Schema not found: 2281_nonexistent_1.0, status=404 Not Found, title=Not Found}");

		when(objectMapperMock.readTree(jsonValue)).thenReturn(jsonNodeMock);
		doThrow(notFoundError).when(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonNodeMock);

		// Act
		final var exception = assertThrows(ClientProblem.class,
			() -> jsonSchemaValidationService.validate(municipalityId, schemaId, jsonValue));

		// Assert
		assertThat(exception).isSameAs(notFoundError);

		verify(objectMapperMock).readTree(jsonValue);
		verify(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonNodeMock);
	}

	@Test
	void validateWithServerError() throws JsonProcessingException {
		// Arrange
		final var municipalityId = "2281";
		final var schemaId = "2281_schema_1.0";
		final var jsonValue = "{\"productId\": 1}";
		final var serverError = new ServerProblem(BAD_GATEWAY, "json-schema error: {detail=Internal Server Error, status=500 Internal Server Error, title=Internal Server Error}");

		when(objectMapperMock.readTree(jsonValue)).thenReturn(jsonNodeMock);
		doThrow(serverError).when(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonNodeMock);

		// Act
		final var exception = assertThrows(ServerProblem.class,
			() -> jsonSchemaValidationService.validate(municipalityId, schemaId, jsonValue));

		// Assert
		assertThat(exception).isSameAs(serverError);

		verify(objectMapperMock).readTree(jsonValue);
		verify(jsonSchemaClientMock).validateJson(municipalityId, schemaId, jsonNodeMock);
	}

	@Test
	void validateWithInvalidJson() throws JsonProcessingException {
		// Arrange
		final var municipalityId = "2281";
		final var schemaId = "2281_schema_1.0";
		final var invalidJson = "not valid json";

		when(objectMapperMock.readTree(invalidJson)).thenThrow(new JsonProcessingException("Unexpected character") {});

		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> jsonSchemaValidationService.validate(municipalityId, schemaId, invalidJson));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(exception.getMessage()).contains("Invalid JSON");

		verify(objectMapperMock).readTree(invalidJson);
	}
}
