package se.sundsvall.partyassets.api.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
import static org.zalando.problem.Status.BAD_GATEWAY;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.exception.ServerProblem;
import se.sundsvall.partyassets.api.model.AssetJsonParameter;
import se.sundsvall.partyassets.service.JsonSchemaValidationService;

@ExtendWith(MockitoExtension.class)
class ValidJsonParameterConstraintValidatorTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Mock
	private ConstraintValidatorContext constraintValidatorContextMock;

	@Mock
	private ConstraintViolationBuilder constraintViolationBuilderMock;

	@Mock
	private JsonSchemaValidationService jsonSchemaValidationServiceMock;

	@Mock
	private RequestAttributes requestAttributesMock;

	@InjectMocks
	private ValidJsonParameterConstraintValidator validator;

	@BeforeEach
	void setUp() {
		RequestContextHolder.setRequestAttributes(requestAttributesMock);
	}

	private void setupMunicipalityId() {
		when(requestAttributesMock.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, SCOPE_REQUEST))
			.thenReturn(Map.of("municipalityId", MUNICIPALITY_ID));
	}

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void validateValidJson() {
		// Arrange
		setupMunicipalityId();
		final var key = "key";
		final var schemaId = "schemaId";
		final var json = OBJECT_MAPPER.createObjectNode();
		final var assetJsonParameter = AssetJsonParameter.create()
			.withKey(key)
			.withSchemaId(schemaId)
			.withValue(json);

		doNothing().when(jsonSchemaValidationServiceMock).validate(MUNICIPALITY_ID, schemaId, json);

		// Act
		final var result = validator.isValid(assetJsonParameter, constraintValidatorContextMock);

		// Assert
		assertThat(result).isTrue();
		verify(jsonSchemaValidationServiceMock).validate(MUNICIPALITY_ID, schemaId, json);
		verifyNoInteractions(constraintViolationBuilderMock);
	}

	@Test
	void validateInvalidJsonClientProblem() {
		// Arrange
		setupMunicipalityId();
		final var key = "key";
		final var schemaId = "schemaId";
		final var json = OBJECT_MAPPER.createObjectNode().put("invalid", true);
		final var assetJsonParameter = AssetJsonParameter.create()
			.withKey(key)
			.withSchemaId(schemaId)
			.withValue(json);

		when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilderMock);
		doThrow(new ClientProblem(BAD_GATEWAY, "json-schema error: {detail=Validation error, status=400 Bad Request, title=Bad Request}"))
			.when(jsonSchemaValidationServiceMock).validate(MUNICIPALITY_ID, schemaId, json);

		// Act
		final var result = validator.isValid(assetJsonParameter, constraintValidatorContextMock);

		// Assert
		assertThat(result).isFalse();
		verify(jsonSchemaValidationServiceMock).validate(MUNICIPALITY_ID, schemaId, json);
		verify(constraintValidatorContextMock).disableDefaultConstraintViolation();
		verify(constraintValidatorContextMock).buildConstraintViolationWithTemplate("Bad Request: Validation error");
		verify(constraintViolationBuilderMock).addConstraintViolation();
	}

	@Test
	void validateServerProblemIsRethrown() {
		// Arrange
		setupMunicipalityId();
		final var key = "key";
		final var schemaId = "schemaId";
		final var json = OBJECT_MAPPER.createObjectNode();
		final var assetJsonParameter = AssetJsonParameter.create()
			.withKey(key)
			.withSchemaId(schemaId)
			.withValue(json);

		final var serverProblem = new ServerProblem(BAD_GATEWAY, "json-schema error: {detail=Internal Server Error, status=500 Internal Server Error, title=Internal Server Error}");
		doThrow(serverProblem)
			.when(jsonSchemaValidationServiceMock).validate(MUNICIPALITY_ID, schemaId, json);

		// Act & Assert
		assertThatThrownBy(() -> validator.isValid(assetJsonParameter, constraintValidatorContextMock))
			.isSameAs(serverProblem);

		verify(jsonSchemaValidationServiceMock).validate(MUNICIPALITY_ID, schemaId, json);
		verifyNoInteractions(constraintViolationBuilderMock);
	}

	@Test
	void validateNullAssetJsonParameter() {
		// Arrange
		final var assetJsonParameter = (AssetJsonParameter) null;

		when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilderMock);

		// Act
		final var result = validator.isValid(assetJsonParameter, constraintValidatorContextMock);

		// Assert
		assertThat(result).isFalse();
		verify(jsonSchemaValidationServiceMock, never()).validate(anyString(), anyString(), any(JsonNode.class));
		verify(constraintValidatorContextMock).disableDefaultConstraintViolation();
		verify(constraintValidatorContextMock).buildConstraintViolationWithTemplate("Value is null");
		verify(constraintViolationBuilderMock).addConstraintViolation();
	}

	@Test
	void validateNullAssetJsonParameterWhenNullableIsTrue() {
		// Arrange
		final var assetJsonParameter = (AssetJsonParameter) null;
		ReflectionTestUtils.setField(validator, "nullable", true);

		// Act
		final var result = validator.isValid(assetJsonParameter, constraintValidatorContextMock);

		// Assert
		assertThat(result).isTrue();
		verify(jsonSchemaValidationServiceMock, never()).validate(anyString(), anyString(), any(JsonNode.class));
		verifyNoInteractions(constraintViolationBuilderMock);
	}

	@Test
	void validateThrowsException() {
		// Arrange
		setupMunicipalityId();
		final var key = "key";
		final var schemaId = "schemaId";
		final var json = OBJECT_MAPPER.createObjectNode();
		final var assetJsonParameter = AssetJsonParameter.create()
			.withKey(key)
			.withSchemaId(schemaId)
			.withValue(json);

		when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilderMock);
		doThrow(new RuntimeException("some error message"))
			.when(jsonSchemaValidationServiceMock).validate(MUNICIPALITY_ID, schemaId, json);

		// Act
		final var result = validator.isValid(assetJsonParameter, constraintValidatorContextMock);

		// Assert
		assertThat(result).isFalse();
		verify(jsonSchemaValidationServiceMock).validate(MUNICIPALITY_ID, schemaId, json);
		verify(constraintValidatorContextMock).disableDefaultConstraintViolation();
		verify(constraintValidatorContextMock).buildConstraintViolationWithTemplate("some error message");
		verify(constraintViolationBuilderMock).addConstraintViolation();
	}

	@Test
	void validateWithMissingMunicipalityId() {
		// Arrange
		RequestContextHolder.resetRequestAttributes();

		final var key = "key";
		final var schemaId = "schemaId";
		final var json = OBJECT_MAPPER.createObjectNode();
		final var assetJsonParameter = AssetJsonParameter.create()
			.withKey(key)
			.withSchemaId(schemaId)
			.withValue(json);

		when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilderMock);

		// Act
		final var result = validator.isValid(assetJsonParameter, constraintValidatorContextMock);

		// Assert
		assertThat(result).isFalse();
		verify(constraintValidatorContextMock).disableDefaultConstraintViolation();
		verify(constraintValidatorContextMock).buildConstraintViolationWithTemplate("municipalityId not found in request path");
		verify(constraintViolationBuilderMock).addConstraintViolation();
	}
}
