package se.sundsvall.partyassets.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.partyassets.service.AssetEventService;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;

@RestController
@Validated
@RequestMapping(value = "/{municipalityId}/asset-events")
@Tag(name = "Asset events")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class AssetEventResource {

	private final AssetEventService service;

	AssetEventResource(final AssetEventService service) {
		this.service = service;
	}

	@PostMapping(path = "republish", produces = ALL_VALUE)
	@Operation(summary = "Publish all assets again",
		description = "Publishes every asset of the municipality as it is now, as an UPDATED event at its current version. Used to load a new consumer, or to recover one. Returns when all are published",
		responses = {
			@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true),
			@ApiResponse(responseCode = "503", description = "Service Unavailable - RabbitMQ is off", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	ResponseEntity<Void> republish(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId) {

		service.republish(municipalityId);
		return noContent().build();
	}
}
