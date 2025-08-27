package se.sundsvall.partyassets.service;

import static com.networknt.schema.InputFormat.JSON;
import static com.networknt.schema.SpecVersion.VersionFlag.V202012;
import static java.util.Collections.emptySet;
import static java.util.Locale.ENGLISH;
import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.BAD_REQUEST;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

@Service
public class JsonSchemaValidationService {

	private static final Locale LOCALE = ENGLISH;

	/**
	 * Creates a schema that will use Draft 2020-12 as the default if $schema is not specified
	 * in the schema data. If $schema is specified in the schema data then that schema dialect will be used instead.
	 * 
	 * @param  schemaAsString The JSON schema as a string.
	 * @return                a JsonSchema
	 */
	public JsonSchema toJsonSchema(String schemaAsString) {
		return JsonSchemaFactory.getInstance(V202012).getSchema(schemaAsString);
	}

	/**
	 * Validates input JSON against a JSON schema.
	 * 
	 * @param  input  the JSON to validate
	 * @param  schema the JsonSchema to use in validation
	 * @return        a set of ValidationMessages if input is invalid. An empty set if input is valid.
	 */
	public Set<ValidationMessage> validate(String input, JsonSchema schema) {
		final var assertions = schema.validate(input, JSON, executionContext -> {
			// By default since Draft 2019-09 the format keyword only generates annotations and not assertions
			executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
			// Set Locale to get localized error messages.
			executionContext.getExecutionConfig().setLocale(LOCALE);
		});

		return ofNullable(assertions).orElse(emptySet());
	}

	/**
	 * Validates input JSON against a JSON schema.
	 * 
	 * @param  input                      the JSON to validate
	 * @param  schema                     the JsonSchema to use in validation
	 * @throws ConstraintViolationProblem with BAD_REUEST if the JSON is not valid.
	 */
	public void validateAndThrow(String input, JsonSchema schema) {
		final var violations = validate(input, schema).stream()
			.map(message -> new Violation(message.getInstanceLocation().toString(), message.getError()))
			.toList();

		if (!violations.isEmpty()) {
			throw new ConstraintViolationProblem(BAD_REQUEST, violations);
		}
	}
}
