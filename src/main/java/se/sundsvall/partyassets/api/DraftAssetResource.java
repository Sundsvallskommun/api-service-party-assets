package se.sundsvall.partyassets.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.partyassets.api.model.Asset;
import se.sundsvall.partyassets.api.model.AssetCreateRequest;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.api.model.DraftAssetUpdateRequest;
import se.sundsvall.partyassets.service.AssetService;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.dept44.problem.Problem.badRequest;
import static se.sundsvall.partyassets.api.model.Status.DRAFT;

@RestController
@Validated
@RequestMapping(value = "/{municipalityId}/asset-drafts")
@Tag(name = "Draft Assets")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class DraftAssetResource {

	private final AssetService service;

	DraftAssetResource(final AssetService service) {
		this.service = service;
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get draft assets", responses = {
		@ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
	})
	ResponseEntity<List<Asset>> getDraftAssets(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@ParameterObject @Valid final AssetSearchRequest request) {

		return ok(service.getDraftAssets(municipalityId, request));
	}

	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	@Operation(summary = "Create a draft asset", responses = {
		@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource."), useReturnTypeSchema = true)
	})
	ResponseEntity<Void> createDraftAsset(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "sourceReference",
			description = "Relation reference in format (only source is used): '{relationType}|{sourceResourceId};{sourceType};{sourceService};{sourceNamespace}|{targetResourceId};{targetType};{targetService};{targetNamespace}'",
			example = "LINK|1234;case;service;MY_NAMESPACE|") @RequestParam(required = false) final String sourceReference,
		@Valid @RequestBody final AssetCreateRequest asset) {

		// Make sure that the asset status isn't set to DRAFT
		if (asset.getStatus() != DRAFT) {
			throw badRequest("Status {0} is not allowed when creating draft assets", asset.getStatus());
		}

		final var result = service.createAsset(municipalityId, asset, sourceReference);
		return created(fromPath("/" + municipalityId + "/asset-drafts/{id}").buildAndExpand(result).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@PatchMapping(path = "{id}", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	@Operation(summary = "Update a draft asset", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Void> updateDraftAsset(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable @ValidUuid final String id, @Valid @RequestBody final DraftAssetUpdateRequest asset) {

		service.updateAsset(municipalityId, id, asset);
		return noContent().build();
	}
}
