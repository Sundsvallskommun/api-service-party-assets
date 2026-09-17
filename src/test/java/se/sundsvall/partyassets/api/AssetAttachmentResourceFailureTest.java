package se.sundsvall.partyassets.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.service.AssetAttachmentService;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.TEXT_HTML;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class AssetAttachmentResourceFailureTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ASSET_ID = randomUUID().toString();
	private static final String INVALID_MUNICIPALITY_ID = "invalid-municipality-id";
	private static final String INVALID_UUID = "not-a-valid-uuid";

	@MockitoBean
	private AssetAttachmentService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	private static ByteArrayResource file() {
		return new ByteArrayResource("content".getBytes()) {
			@Override
			public String getFilename() {
				return "lokalritning.pdf";
			}
		};
	}

	private static String path(final String municipalityId, final String assetId) {
		return "/" + municipalityId + "/assets/" + assetId + "/attachments";
	}

	@Test
	void createAttachmentWithInvalidMunicipalityId() {
		final var body = new MultipartBodyBuilder();
		body.part("attachment", file()).contentType(APPLICATION_PDF);

		final var response = webTestClient.post()
			.uri(path(INVALID_MUNICIPALITY_ID, ASSET_ID))
			.contentType(MULTIPART_FORM_DATA)
			.bodyValue(body.build())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("createAttachment.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(serviceMock);
	}

	@Test
	void createAttachmentWithInvalidContentType() {
		final var body = new MultipartBodyBuilder();
		body.part("attachment", file()).contentType(TEXT_HTML);

		final var response = webTestClient.post()
			.uri(path(MUNICIPALITY_ID, ASSET_ID))
			.contentType(MULTIPART_FORM_DATA)
			.bodyValue(body.build())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("createAttachment.attachment", "content type is not allowed for attachments"));

		verifyNoInteractions(serviceMock);
	}

	@Test
	void readAttachmentsWithInvalidAssetId() {
		final var response = webTestClient.get()
			.uri(path(MUNICIPALITY_ID, INVALID_UUID))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("readAttachments.id", "not a valid UUID"));

		verifyNoInteractions(serviceMock);
	}

	@Test
	void readAttachmentWithInvalidAttachmentId() {
		final var response = webTestClient.get()
			.uri(path(MUNICIPALITY_ID, ASSET_ID) + "/" + INVALID_UUID)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("readAttachment.attachmentId", "not a valid UUID"));

		verifyNoInteractions(serviceMock);
	}

	@Test
	void deleteAttachmentWithInvalidAttachmentId() {
		final var response = webTestClient.delete()
			.uri(path(MUNICIPALITY_ID, ASSET_ID) + "/" + INVALID_UUID)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("deleteAttachment.attachmentId", "not a valid UUID"));

		verifyNoInteractions(serviceMock);
	}

	@Test
	void updateAttachmentWithInvalidAttachmentId() {
		final var response = webTestClient.patch()
			.uri(path(MUNICIPALITY_ID, ASSET_ID) + "/" + INVALID_UUID)
			.contentType(APPLICATION_JSON)
			.bodyValue("{}")
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("updateAttachment.attachmentId", "not a valid UUID"));

		verifyNoInteractions(serviceMock);
	}

	@Test
	void createAttachmentWithTooLongCategory() {
		final var body = new MultipartBodyBuilder();
		body.part("attachment", file()).contentType(APPLICATION_PDF);
		body.part("category", "a".repeat(256));

		final var response = webTestClient.post()
			.uri(path(MUNICIPALITY_ID, ASSET_ID))
			.contentType(MULTIPART_FORM_DATA)
			.bodyValue(body.build())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field)
			.containsExactly("createAttachment.category");

		verifyNoInteractions(serviceMock);
	}

	@Test
	void createAttachmentWithoutFilePart() {
		final var body = new MultipartBodyBuilder();
		body.part("category", "LOKALRITNING");

		webTestClient.post()
			.uri(path(MUNICIPALITY_ID, ASSET_ID))
			.contentType(MULTIPART_FORM_DATA)
			.bodyValue(body.build())
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}
}
