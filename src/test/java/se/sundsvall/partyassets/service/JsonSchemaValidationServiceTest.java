package se.sundsvall.partyassets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.networknt.schema.ValidationMessage;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.partyassets.integration.db.JsonSchemaRepository;
import se.sundsvall.partyassets.integration.db.model.JsonSchemaEntity;

@SpringBootTest(classes = JsonSchemaValidationService.class)
@ActiveProfiles(value = "junit")
@ExtendWith(ResourceLoaderExtension.class)
class JsonSchemaValidationServiceTest {

	private static final String SCHEMA = "files/jsonschema/schema.json";
	private static final String VALID_JSON = "files/jsonschema/valid_json.json";
	private static final String INVALID_JSON_MISSING_ALL_PROPERTIES = "files/jsonschema/invalid_json_missing_all_properties.json";
	private static final String INVALID_JSON_BAD_DATATYPE_ON_PROPERTY = "files/jsonschema/invalid_json_bad_datatype_on_property.json";
	private static final String INVALID_JSON_NON_UNIQUE_TAGS = "files/jsonschema/invalid_json_non_unique_tags.json";
	private static final String INVALID_JSON_MISC_ERRORS = "files/jsonschema/invalid_json_misc_errors.json";

	@MockitoBean
	private JsonSchemaRepository jsonSchemaRepositoryMock;

	@Autowired
	private JsonSchemaValidationService jsonSchemaValidationService;

	@Test
	void validateWithValidJson(@Load(SCHEMA) final String schema, @Load(VALID_JSON) final String json) {

		// Act
		final var jsonSchema = jsonSchemaValidationService.toJsonSchema(schema);
		final var validationMessages = jsonSchemaValidationService.validate(json, jsonSchema);

		// Assert
		assertThat(validationMessages).isEmpty();
	}

	@Test
	void validateWithAllMissingProperties(@Load(SCHEMA) final String schema, @Load(INVALID_JSON_MISSING_ALL_PROPERTIES) final String json) {

		// Act
		final var jsonSchema = jsonSchemaValidationService.toJsonSchema(schema);
		final var validationMessages = jsonSchemaValidationService.validate(json, jsonSchema);

		// Assert
		assertThat(validationMessages)
			.isNotEmpty()
			.extracting(ValidationMessage::getMessage)
			.containsExactly(
				"$: required property 'productId' not found",
				"$: required property 'productName' not found",
				"$: required property 'price' not found");
	}

	@Test
	void validateWithBadDatatypeOnProperty(@Load(SCHEMA) final String schema, @Load(INVALID_JSON_BAD_DATATYPE_ON_PROPERTY) final String json) {

		// Act
		final var jsonSchema = jsonSchemaValidationService.toJsonSchema(schema);
		final var validationMessages = jsonSchemaValidationService.validate(json, jsonSchema);

		// Assert
		assertThat(validationMessages)
			.isNotEmpty()
			.extracting(ValidationMessage::getMessage)
			.containsExactly("$.productId: string found, integer expected");
	}

	@Test
	void validateWithNonUniqueTags(@Load(SCHEMA) final String schema, @Load(INVALID_JSON_NON_UNIQUE_TAGS) final String json) {

		// Act
		final var jsonSchema = jsonSchemaValidationService.toJsonSchema(schema);
		final var validationMessages = jsonSchemaValidationService.validate(json, jsonSchema);

		// Assert
		assertThat(validationMessages)
			.isNotEmpty()
			.extracting(ValidationMessage::getMessage)
			.containsExactly("$.tags: must have only unique items in the array");
	}

	@Test
	void validateWithMiscErrors(@Load(SCHEMA) final String schema, @Load(INVALID_JSON_MISC_ERRORS) final String json) {

		// Act
		final var jsonSchema = jsonSchemaValidationService.toJsonSchema(schema);
		final var validationMessages = jsonSchemaValidationService.validate(json, jsonSchema);

		// Assert
		assertThat(validationMessages)
			.isNotEmpty()
			.extracting(ValidationMessage::getMessage)
			.containsExactly(
				"$.price: must have an exclusive minimum value of 0",
				"$.tags[5]: integer found, string expected",
				"$.tags: must have only unique items in the array",
				"$: required property 'productName' not found");
	}

	@Test
	void validateAndThrowWithMiscErrors(@Load(SCHEMA) final String schema, @Load(INVALID_JSON_MISC_ERRORS) final String json) {

		// Act
		final var jsonSchema = jsonSchemaValidationService.toJsonSchema(schema);
		final var exception = assertThrows(ConstraintViolationProblem.class, () -> jsonSchemaValidationService.validateAndThrow(json, jsonSchema));

		// Assert
		assertThat(exception)
			.isNotNull()
			.hasMessage("Constraint Violation");

		assertThat(exception.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(
				tuple("$.price", "must have an exclusive minimum value of 0"),
				tuple("$.tags[5]", "integer found, string expected"),
				tuple("$.tags", "must have only unique items in the array"),
				tuple("$", "required property 'productName' not found"));
	}

	@Test
	void validateAndThrowWithValidJson(@Load(SCHEMA) final String schema, @Load(VALID_JSON) final String json) {

		// Act
		final var jsonSchema = jsonSchemaValidationService.toJsonSchema(schema);
		assertDoesNotThrow(() -> jsonSchemaValidationService.validateAndThrow(json, jsonSchema));
	}

	@Test
	void validateBySchemaIdWithValidJson(@Load(SCHEMA) final String schema, @Load(VALID_JSON) final String json) {

		// Arrange
		final var schemaId = "schemaId";
		when(jsonSchemaRepositoryMock.findById(schemaId)).thenReturn(Optional.of(JsonSchemaEntity.create()
			.withId(schemaId)
			.withValue(schema)));

		// Act
		final var validationMessages = jsonSchemaValidationService.validate(json, schemaId);

		// Assert
		assertThat(validationMessages).isEmpty();
		verify(jsonSchemaRepositoryMock).findById(schemaId);
	}

	@Test
	void validateBySchemaIdWithMiscErrors(@Load(SCHEMA) final String schema, @Load(INVALID_JSON_MISC_ERRORS) final String json) {

		// Arrange
		final var schemaId = "schemaId";
		when(jsonSchemaRepositoryMock.findById(schemaId)).thenReturn(Optional.of(JsonSchemaEntity.create()
			.withId(schemaId)
			.withValue(schema)));

		// Act
		final var validationMessages = jsonSchemaValidationService.validate(json, schemaId);

		// Assert
		assertThat(validationMessages)
			.isNotEmpty()
			.extracting(ValidationMessage::getMessage)
			.containsExactly(
				"$.price: must have an exclusive minimum value of 0",
				"$.tags[5]: integer found, string expected",
				"$.tags: must have only unique items in the array",
				"$: required property 'productName' not found");

		verify(jsonSchemaRepositoryMock).findById(schemaId);
	}

	@Test
	void validateAndThrowBySchemaIdWithValidJson(@Load(SCHEMA) final String schema, @Load(VALID_JSON) final String json) {

		// Arrange
		final var schemaId = "schemaId";
		when(jsonSchemaRepositoryMock.findById(schemaId)).thenReturn(Optional.of(JsonSchemaEntity.create()
			.withId(schemaId)
			.withValue(schema)));

		// Act
		assertDoesNotThrow(() -> jsonSchemaValidationService.validateAndThrow(json, schemaId));

		// Assert
		verify(jsonSchemaRepositoryMock).findById(schemaId);
	}

	@Test
	void validateAndThrowBySchemaIdWithMiscErrors(@Load(SCHEMA) final String schema, @Load(INVALID_JSON_MISC_ERRORS) final String json) {

		// Arrange
		final var schemaId = "schemaId";
		when(jsonSchemaRepositoryMock.findById(schemaId)).thenReturn(Optional.of(JsonSchemaEntity.create()
			.withId(schemaId)
			.withValue(schema)));

		// Act
		final var exception = assertThrows(ConstraintViolationProblem.class, () -> jsonSchemaValidationService.validateAndThrow(json, schemaId));

		// Assert
		assertThat(exception)
			.isNotNull()
			.hasMessage("Constraint Violation");

		assertThat(exception.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(
				tuple("$.price", "must have an exclusive minimum value of 0"),
				tuple("$.tags[5]", "integer found, string expected"),
				tuple("$.tags", "must have only unique items in the array"),
				tuple("$", "required property 'productName' not found"));
	}
}
