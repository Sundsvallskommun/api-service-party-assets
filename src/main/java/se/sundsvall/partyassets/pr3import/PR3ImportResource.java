package se.sundsvall.partyassets.pr3import;

import generated.se.sundsvall.messaging.EmailAttachment;
import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.EmailSender;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.partyassets.pr3import.PR3ImportProperties.Sender;

import static java.util.Optional.ofNullable;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@ConditionalOnProperty(name = "pr3import.enabled", havingValue = "true", matchIfMissing = true)
@Validated
@RequestMapping(value = "/{municipalityId}/pr3import")
@Tag(name = "PR3 Import")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class PR3ImportResource {

	static final String CONTENT_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	private final PR3Importer importer;

	private final PR3ImportMessagingClient messagingClient;

	private final PR3ImportProperties properties;

	PR3ImportResource(final PR3Importer importer, final PR3ImportMessagingClient messagingClient, final PR3ImportProperties properties) {
		this.importer = importer;
		this.messagingClient = messagingClient;
		this.properties = properties;
	}

	@PostMapping(consumes = MULTIPART_FORM_DATA_VALUE, produces = {
		APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	PR3Importer.Result handleImport(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestParam("file") final MultipartFile file,
		@RequestParam("email") final String emailAddress) throws IOException {
		final var result = importer.importFromExcel(file.getInputStream(), municipalityId);

		final var message = String.format("Totalt %d post(er) varav %d lyckad(e) och %d misslyckade", result.getTotal(), result.getSuccessful(), result.getFailed());
		final var emailRequest = new EmailRequest()
			.emailAddress(emailAddress)
			.subject("PR3 Import")
			.message(message);

		if (result.getFailed() > 0) {
			emailRequest.setAttachments(List.of(new EmailAttachment()
				.name("FAILED-" + file.getOriginalFilename())
				.contentType(CONTENT_TYPE_EXCEL)
				.content(Base64.getEncoder().encodeToString(result.getFailedExcelData()))));
		}

		ofNullable(properties.senders().get(municipalityId)).ifPresent(sender -> emailRequest.setSender(toEmailSender(sender)));

		messagingClient.sendEmail(municipalityId, emailRequest);

		return result;
	}

	private EmailSender toEmailSender(final Sender sender) {
		return sender == null ? null : new EmailSender().address(sender.email()).name(sender.name());
	}

}
