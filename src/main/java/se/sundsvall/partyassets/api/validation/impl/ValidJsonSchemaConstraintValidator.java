package se.sundsvall.partyassets.api.validation.impl;

import static com.networknt.schema.SpecificationVersion.DRAFT_2020_12;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.DialectId;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Optional;
import org.apache.commons.lang3.Strings;
import org.springframework.util.StringUtils;
import se.sundsvall.partyassets.api.validation.ValidJsonSchema;
import se.sundsvall.partyassets.service.JsonSchemaValidationService;

public class ValidJsonSchemaConstraintValidator implements ConstraintValidator<ValidJsonSchema, String> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final String SUPPORTED_SCHEMA_SPECIFICATION = DialectId.DRAFT_2020_12;

	private final JsonSchemaValidationService jsonSchemaValidationService;
	private boolean nullable;

	public ValidJsonSchemaConstraintValidator(JsonSchemaValidationService jsonSchemaValidationService) {
		this.jsonSchemaValidationService = jsonSchemaValidationService;
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
		if (!hasSupportedSpecification(inputJsonSchema, context)) {
			return false;
		}

		// Assert JSON-schema against meta schema.
		final var metaSchema = SchemaRegistry
			.withDefaultDialect(DRAFT_2020_12)
			.getSchema(SchemaLocation.of(SUPPORTED_SCHEMA_SPECIFICATION));

		final var validationMessages = jsonSchemaValidationService.validate(inputJsonSchema, metaSchema);

		validationMessages.forEach(message -> addViolation(Optional.ofNullable(message.getInstanceLocation()).map(Object::toString).filter(StringUtils::hasText).map(value -> value + ": ").orElse("") + message.getMessage(), context));

		return validationMessages.isEmpty();
	}

	// ---- Private helpers ------------------------------------------------------

	private void addViolation(String value, ConstraintValidatorContext constraintContext) {
		constraintContext.disableDefaultConstraintViolation();
		constraintContext.buildConstraintViolationWithTemplate(value).addConstraintViolation();
	}

	private boolean hasSupportedSpecification(String inputJsonSchema, ConstraintValidatorContext constraintContext) {
		try {
			final var jsonSchema = this.jsonSchemaValidationService.toJsonSchema(inputJsonSchema);
			final var schemaNodeValue = Optional.ofNullable(jsonSchema.getSchemaNode().get("$schema"))
				.map(JsonNode::asText)
				.orElse(null);

			if (!Strings.CI.equals(schemaNodeValue, SUPPORTED_SCHEMA_SPECIFICATION)) {
				addViolation("Wrong value in $schema-node. Expected: '%s' Found: '%s'".formatted(SUPPORTED_SCHEMA_SPECIFICATION, schemaNodeValue), constraintContext);
				return false;
			}
		} catch (Exception e) {
			addViolation("Could not determine schema specification: [%s]".formatted(e.getMessage()), constraintContext);
			return false;
		}
		return true;
	}

	private boolean isValidJson(String json, ConstraintValidatorContext context) {
		if (isBlank(json)) {
			addViolation("must be valid JSON, but was blank", context);
			return false;
		}
		try {
			OBJECT_MAPPER.readTree(json);
			return true;
		} catch (Exception e) {
			addViolation("must be valid JSON, but was: '%s'".formatted(json), context);
			return false;
		}
	}
}
