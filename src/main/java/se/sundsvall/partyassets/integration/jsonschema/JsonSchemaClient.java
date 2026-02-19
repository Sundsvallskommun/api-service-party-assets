package se.sundsvall.partyassets.integration.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.partyassets.integration.jsonschema.configuration.JsonSchemaConfiguration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.partyassets.integration.jsonschema.configuration.JsonSchemaConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.json-schema.url}", configuration = JsonSchemaConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface JsonSchemaClient {

	/**
	 * Validate a JSON structure against the specified schema.
	 *
	 * @param  municipalityId                       the municipality ID.
	 * @param  id                                   the schema ID (format: municipalityId_name_version).
	 * @param  jsonNode                             the JSON to validate.
	 * @throws org.zalando.problem.ThrowableProblem if validation fails or schema not found.
	 */
	@PostMapping(path = "/{municipalityId}/schemas/{id}/validation", consumes = APPLICATION_JSON_VALUE)
	void validateJson(@PathVariable String municipalityId, @PathVariable String id, @RequestBody JsonNode jsonNode);
}
