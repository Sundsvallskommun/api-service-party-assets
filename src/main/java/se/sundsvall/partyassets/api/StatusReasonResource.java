package se.sundsvall.partyassets.api;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.service.StatusService;

@RestController
@Validated
@RequestMapping(value = "/statusreasons")
@Tag(name = "Statusreasons")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class StatusReasonResource {

	private final StatusService service;

	public StatusReasonResource(final StatusService service) {
		this.service = service;
	}

	@GetMapping(produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
	public ResponseEntity<Map<Status, List<String>>> readAllReasons() {
		return ResponseEntity.ok(service.getReasonsForAllStatuses());
	}

	@GetMapping(path = "{status}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
	public ResponseEntity<List<String>> readReasons(@PathVariable final Status status) {
		return ResponseEntity.ok(service.getReasons(status));
	}

	@PostMapping(path = "{status}", consumes = APPLICATION_JSON_VALUE, produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource."))
	public ResponseEntity<Void> createReasons(@PathVariable final Status status, @RequestBody @NotEmpty final List<@NotBlank String> statusReasons, final UriComponentsBuilder uriComponentsBuilder) {
		service.createReasons(status, statusReasons);
		return ResponseEntity
			.created(uriComponentsBuilder
				.path("/statusreasons/{status}")
				.buildAndExpand(status)
				.toUri())
			.build();
	}

	@DeleteMapping(path = "{status}", produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	public ResponseEntity<Void> deleteAsset(@PathVariable final Status status) {
		service.deleteReasons(status);
		return ResponseEntity.noContent().build();
	}
}
