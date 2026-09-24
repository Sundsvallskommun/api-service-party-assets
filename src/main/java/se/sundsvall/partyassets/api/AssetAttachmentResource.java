package se.sundsvall.partyassets.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.partyassets.api.model.AssetAttachment;
import se.sundsvall.partyassets.api.model.AssetAttachmentUpdateRequest;
import se.sundsvall.partyassets.service.AssetAttachmentService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

@RestController
@Validated
@RequestMapping(value = "/{municipalityId}/assets/{id}/attachments")
@Tag(name = "Asset attachments")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class AssetAttachmentResource {

	private final AssetAttachmentService service;

	AssetAttachmentResource(final AssetAttachmentService service) {
		this.service = service;
	}

	@PostMapping(consumes = MULTIPART_FORM_DATA_VALUE, produces = ALL_VALUE)
	@Operation(summary = "Create an asset attachment", responses = {
		@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource."), useReturnTypeSchema = true),
		@ApiResponse(responseCode = "409", description = "Conflict - The asset was updated by someone else", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Void> createAttachment(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable @ValidUuid final String id,
		@NotNull @RequestPart("attachment") final MultipartFile attachment,
		@Parameter(name = "category", description = "What the attachment depicts", example = "LOKALRITNING") @Size(max = 255) @RequestPart(name = "category", required = false) final String category,
		@Parameter(name = "description", description = "Attachment description") @Size(max = 255) @RequestPart(name = "description", required = false) final String description) {

		final var attachmentId = service.createAttachment(municipalityId, id, attachment, category, description);

		return created(fromPath("/{municipalityId}/assets/{id}/attachments/{attachmentId}").buildAndExpand(municipalityId, id, attachmentId).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get asset attachments", responses = {
		@ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
	})
	ResponseEntity<List<AssetAttachment>> readAttachments(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable @ValidUuid final String id) {

		return ok(service.readAttachments(municipalityId, id));
	}

	@GetMapping(path = "{attachmentId}", produces = APPLICATION_OCTET_STREAM_VALUE)
	@Operation(summary = "Download an asset attachment", responses = {
		@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = APPLICATION_OCTET_STREAM_VALUE, schema = @Schema(type = "string", format = "binary")))
	})
	ResponseEntity<byte[]> readAttachment(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable @ValidUuid final String id,
		@PathVariable @ValidUuid final String attachmentId) {

		final var attachment = service.readAttachment(municipalityId, id, attachmentId);

		return ok()
			.header(CONTENT_TYPE, attachment.mimeType())
			.header(CONTENT_DISPOSITION, ContentDisposition.attachment().filename(attachment.fileName(), UTF_8).build().toString())
			.contentLength(attachment.content().length)
			.body(attachment.content());
	}

	@PatchMapping(path = "{attachmentId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Update asset attachment metadata", responses = {
		@ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "409", description = "Conflict - The asset was updated by someone else", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<AssetAttachment> updateAttachment(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable @ValidUuid final String id,
		@PathVariable @ValidUuid final String attachmentId,
		@Valid @RequestBody final AssetAttachmentUpdateRequest request) {

		return ok(service.updateAttachment(municipalityId, id, attachmentId, request));
	}

	@DeleteMapping(path = "{attachmentId}", produces = ALL_VALUE)
	@Operation(summary = "Delete an asset attachment", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "409", description = "Conflict - The asset was updated by someone else", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Void> deleteAttachment(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable @ValidUuid final String id,
		@PathVariable @ValidUuid final String attachmentId) {

		service.deleteAttachment(municipalityId, id, attachmentId);
		return noContent().build();
	}
}
