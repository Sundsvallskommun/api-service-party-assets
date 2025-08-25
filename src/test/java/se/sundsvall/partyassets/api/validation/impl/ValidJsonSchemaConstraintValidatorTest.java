package se.sundsvall.partyassets.api.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.partyassets.service.JsonSchemaService;

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

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private JsonSchemaService jsonSchemaService;

	@InjectMocks
	private ValidJsonSchemaConstraintValidator validator;

	@Test
	void validateValidJsonSchema(@Load(VALID_SCHEMA) final String schema) {
		assertThat(validator.isValid(schema, constraintValidatorContextMock)).isTrue();
	}

	@Test
	void validateInvalidJsonSchemaWhenNotCompliant(@Load(INVALID_SCHEMA_WRONG_TYPE) final String schema) {
		when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilderMock);
		assertThat(validator.isValid(schema, constraintValidatorContextMock)).isFalse();
	}

	@Test
	void validateInvalidJsonSchemaWhenWrongSchemaSpecification(@Load(INVALID_SCHEMA_WRONG_SPECIFICATION) final String schema) {
		when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilderMock);
		assertThat(validator.isValid(schema, constraintValidatorContextMock)).isFalse();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {
		" ", "	", "{,}", "-"
	})
	void validateInvalidJsonSchemaValues(String input) {
		when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilderMock);
		assertThat(validator.isValid(input, constraintValidatorContextMock)).isFalse();
	}
}
