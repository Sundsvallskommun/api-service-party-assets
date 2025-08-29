package se.sundsvall.partyassets.api.validation.impl;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.networknt.schema.ValidationMessage;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.partyassets.api.model.AssetJsonParameter;
import se.sundsvall.partyassets.service.JsonSchemaValidationService;

@ExtendWith({
	MockitoExtension.class, ResourceLoaderExtension.class
})
class ValidJsonParameterConstraintValidatorTest {

	@Mock
	private ConstraintValidatorContext constraintValidatorContextMock;

	@Mock
	private ConstraintViolationBuilder constraintViolationBuilderMock;

	@Mock
	private JsonSchemaValidationService jsonSchemaValidationServiceMock;

	@InjectMocks
	private ValidJsonParameterConstraintValidator validator;

	@Test
	void validateValidJson() {

		// Arrange
		final var key = "key";
		final var schemaId = "schemaId";
		final var json = "{}";
		final var assetJsonParameter = AssetJsonParameter.create()
			.withKey(key)
			.withSchemaId(schemaId)
			.withValue(json);

		when(jsonSchemaValidationServiceMock.validate(anyString(), anyString())).thenReturn(emptySet());

		// Act
		final var result = validator.isValid(assetJsonParameter, constraintValidatorContextMock);

		// Assert
		assertThat(result).isTrue();
		verify(jsonSchemaValidationServiceMock).validate(json, schemaId);
		verifyNoInteractions(constraintValidatorContextMock, constraintViolationBuilderMock);
	}

	@Test
	void validateInvalidJson() {

		// Arrange
		final var key = "key";
		final var schemaId = "schemaId";
		final var json = "{hello world}";
		final var assetJsonParameter = AssetJsonParameter.create()
			.withKey(key)
			.withSchemaId(schemaId)
			.withValue(json);

		when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilderMock);
		when(jsonSchemaValidationServiceMock.validate(anyString(), anyString())).thenReturn(Set.of(ValidationMessage.builder().message("some error message").build()));

		// Act
		final var result = validator.isValid(assetJsonParameter, constraintValidatorContextMock);

		// Assert
		assertThat(result).isFalse();
		verify(jsonSchemaValidationServiceMock).validate(json, schemaId);
		verify(constraintValidatorContextMock).disableDefaultConstraintViolation();
		verify(constraintValidatorContextMock).buildConstraintViolationWithTemplate("some error message");
		verify(constraintViolationBuilderMock).addConstraintViolation();
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
		verify(jsonSchemaValidationServiceMock, never()).validate(anyString(), anyString());
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
		verify(jsonSchemaValidationServiceMock, never()).validate(anyString(), anyString());
		verifyNoInteractions(constraintValidatorContextMock, constraintViolationBuilderMock);
	}

	@Test
	void validateThrowsException() {

		// Arrange
		final var key = "key";
		final var schemaId = "schemaId";
		final var json = "{hello world}";
		final var assetJsonParameter = AssetJsonParameter.create()
			.withKey(key)
			.withSchemaId(schemaId)
			.withValue(json);

		when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(any())).thenReturn(constraintViolationBuilderMock);
		when(jsonSchemaValidationServiceMock.validate(anyString(), anyString())).thenThrow(new RuntimeException("some error message"));

		// Act
		final var result = validator.isValid(assetJsonParameter, constraintValidatorContextMock);

		// Assert
		assertThat(result).isFalse();
		verify(jsonSchemaValidationServiceMock).validate(json, schemaId);
		verify(constraintValidatorContextMock).disableDefaultConstraintViolation();
		verify(constraintValidatorContextMock).buildConstraintViolationWithTemplate("some error message");
		verify(constraintViolationBuilderMock).addConstraintViolation();
	}
}
