package se.sundsvall.partyassets.api.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.networknt.schema.JsonSchema;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.partyassets.service.JsonSchemaValidationService;

@ExtendWith({
	MockitoExtension.class, ResourceLoaderExtension.class
})
class ValidJsonSchemaConstraintValidatorTest {

	private static final String VALID_SCHEMA = "files/jsonschema/schema.json";
	private static final String INVALID_SCHEMA_WRONG_TYPE = "files/jsonschema/invalid_schema_wrong_type.json";
	private static final String INVALID_SCHEMA_WRONG_SPECIFICATION = "files/jsonschema/invalid_schema_wrong_schema_specification.json";

	@Mock
	private ConstraintValidatorContext constraintValidatorContextMock;

	@Mock
	private ConstraintViolationBuilder constraintViolationBuilderMock;

	@Mock(answer = CALLS_REAL_METHODS)
	private JsonSchemaValidationService jsonSchemaValidationServiceMock;

	@InjectMocks
	private ValidJsonSchemaConstraintValidator validator;

	@Test
	void validateValidJsonSchema(@Load(VALID_SCHEMA) final String schema) {

		// Act
		final var result = validator.isValid(schema, constraintValidatorContextMock);

		// Assert
		assertThat(result).isTrue();
		verify(jsonSchemaValidationServiceMock).toJsonSchema(schema);
		verify(jsonSchemaValidationServiceMock).validate(eq(schema), any(JsonSchema.class));
	}

	@Test
	void validateInvalidJsonSchemaWhenNotCompliant(@Load(INVALID_SCHEMA_WRONG_TYPE) final String schema) {

		// Arrange
		when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilderMock);

		// Act
		final var result = validator.isValid(schema, constraintValidatorContextMock);

		// Assert
		assertThat(result).isFalse();
		verify(jsonSchemaValidationServiceMock).toJsonSchema(schema);
		verify(jsonSchemaValidationServiceMock).validate(eq(schema), any(JsonSchema.class));
	}

	@Test
	void validateInvalidJsonSchemaWhenWrongSchemaSpecification(@Load(INVALID_SCHEMA_WRONG_SPECIFICATION) final String schema) {

		// Arrange
		when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilderMock);

		// Act
		final var result = validator.isValid(schema, constraintValidatorContextMock);

		// Assert
		assertThat(result).isFalse();
		verify(jsonSchemaValidationServiceMock).toJsonSchema(schema);
		verify(jsonSchemaValidationServiceMock, never()).validate(any(), anyString());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {
		" ", "	", "{,}", "-"
	})
	void validateInvalidJsonSchemaValues(String input) {

		// Arrange
		when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilderMock);

		// Act
		final var result = validator.isValid(input, constraintValidatorContextMock);

		// Assert
		assertThat(result).isFalse();
		verify(jsonSchemaValidationServiceMock, never()).validate(any(), anyString());
	}
}
