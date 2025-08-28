package se.sundsvall.partyassets.service;

import static com.networknt.schema.InputFormat.JSON;
import static com.networknt.schema.SpecVersion.VersionFlag.V202012;
import static java.util.Locale.ENGLISH;
import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.partyassets.service.Constants.MESSAGE_JSON_SCHEMA_NOT_FOUND;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.partyassets.integration.db.JsonSchemaRepository;
import se.sundsvall.partyassets.integration.db.model.JsonSchemaEntity;

@Service
public class JsonSchemaValidationService {

	private static final Locale LOCALE = ENGLISH;

	private final JsonSchemaRepository jsonSchemaRepository;

	public JsonSchemaValidationService(JsonSchemaRepository jsonSchemaRepository) {
		this.jsonSchemaRepository = jsonSchemaRepository;
	}

	/**
	 * Parses a JSON schema string, defaults to Draft 2020-12 if $schema is not specified.
	 *
	 * @param  schemaAsString JSON schema as string
	 * @return                parsed JsonSchema
	 */
	public JsonSchema toJsonSchema(String schemaAsString) {
		return JsonSchemaFactory.getInstance(V202012).getSchema(schemaAsString);
	}

	/**
	 * Validates input JSON against a schema by ID.
	 *
	 * @param  input    JSON input
	 * @param  schemaId schema ID
	 * @return          validation messages (empty if valid)
	 */
	public Set<ValidationMessage> validate(String input, String schemaId) {
		return validate(input, getSchemaById(schemaId));
	}

	/**
	 * Validates input JSON against a schema.
	 *
	 * @param  input  JSON input
	 * @param  schema JsonSchema
	 * @return        validation messages (empty if valid)
	 */
	public Set<ValidationMessage> validate(String input, JsonSchema schema) {
		return ofNullable(schema.validate(input, JSON, JsonSchemaValidationService::configureExecutionContext))
			.orElseGet(Collections::emptySet);
	}

	/**
	 * Validates input JSON against a schema by ID and throws on errors.
	 *
	 * @param  input                      JSON input
	 * @param  schemaId                   schema ID
	 * @throws ConstraintViolationProblem BAD_REQUEST if input is invalid
	 */
	public void validateAndThrow(String input, String schemaId) {
		validateAndThrow(input, getSchemaById(schemaId));
	}

	/**
	 * Validates input JSON against a schema and throws on errors.
	 *
	 * @param  input                      JSON input
	 * @param  schema                     JsonSchema
	 * @throws ConstraintViolationProblem BAD_REQUEST if input is invalid
	 */
	public void validateAndThrow(String input, JsonSchema schema) {
		final var violations = validate(input, schema).stream()
			.map(message -> new Violation(message.getInstanceLocation().toString(), message.getError()))
			.toList();

		if (!violations.isEmpty()) {
			throw new ConstraintViolationProblem(BAD_REQUEST, violations);
		}
	}

	private JsonSchema getSchemaById(String schemaId) {
		return jsonSchemaRepository.findById(schemaId)
			.map(JsonSchemaEntity::getValue)
			.map(this::toJsonSchema)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, MESSAGE_JSON_SCHEMA_NOT_FOUND.formatted(schemaId)));
	}

	private static void configureExecutionContext(ExecutionContext executionContext) {
		executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
		executionContext.getExecutionConfig().setLocale(LOCALE);
	}
}
