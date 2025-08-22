package se.sundsvall.partyassets.api.validation.impl;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaId;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SpecVersion.VersionFlag;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Optional;
import org.apache.commons.lang3.Strings;
import se.sundsvall.partyassets.api.validation.ValidJsonSchema;
import se.sundsvall.partyassets.service.JsonSchemaService;

public class ValidJsonSchemaConstraintValidator implements ConstraintValidator<ValidJsonSchema, String> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final String SUPPORTED_SCHEMA_SPECIFICATION = SchemaId.V202012;

	private JsonSchemaService jsonSchemaService;
	private boolean nullable;

	public ValidJsonSchemaConstraintValidator(JsonSchemaService jsonSchemaService) {
		this.jsonSchemaService = jsonSchemaService;
	}

	@Override
	public void initialize(final ValidJsonSchema constraintAnnotation) {
		this.nullable = constraintAnnotation.nullable();
	}

	@Override
	public boolean isValid(final String inputJsonSchema, final ConstraintValidatorContext context) {
		if (isNull(inputJsonSchema) && this.nullable) {
			return true;
		}

		// Assert valid JSON
		if (!isValidJson(inputJsonSchema, context)) {
			return false;
		}

		// Assert valid specification version. Defined in SUPPORTED_SCHEMA_SPECIFICATION.
		if (!isValidJsonSchemaSpecificationVersion(inputJsonSchema, context)) {
			return false;
		}

		// Assert JSON-schema against meta schema.
		final var schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(SchemaLocation.of(SUPPORTED_SCHEMA_SPECIFICATION));
		final var assertions = jsonSchemaService.validate(inputJsonSchema, schema);
		assertions.forEach(message -> useCustomMessageForValidation(message.getMessage(), context));

		return assertions.isEmpty();
	}

	private void useCustomMessageForValidation(String value, ConstraintValidatorContext constraintContext) {
		constraintContext.disableDefaultConstraintViolation();
		constraintContext.buildConstraintViolationWithTemplate(value).addConstraintViolation();
	}

	private boolean isValidJsonSchemaSpecificationVersion(String inputJsonSchema, ConstraintValidatorContext constraintContext) {
		try {
			final var jsonSchema = this.jsonSchemaService.toJsonSchema(inputJsonSchema);
			final var schemaNodeValue = Optional.ofNullable(jsonSchema.getSchemaNode().get("$schema"))
				.map(JsonNode::asText)
				.orElse(null);

			if (!Strings.CI.equals(schemaNodeValue, SUPPORTED_SCHEMA_SPECIFICATION)) {
				useCustomMessageForValidation("Wrong value in $schema-node. Should be '%s'".formatted(SUPPORTED_SCHEMA_SPECIFICATION), constraintContext);
				return false;
			}
		} catch (Exception e) {
			useCustomMessageForValidation("Problem identify schema specification: [%s]".formatted(e.getMessage()), constraintContext);
			return false;
		}
		return true;
	}

	private boolean isValidJson(String json, ConstraintValidatorContext constraintContext) {
		try {
			OBJECT_MAPPER.readTree(json);
			return true;
		} catch (Exception e) {
			useCustomMessageForValidation("must be valid JSON", constraintContext);
			return false;
		}
	}
}
