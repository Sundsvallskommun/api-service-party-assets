package se.sundsvall.partyassets.api;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.api.model.AssetAttachment;
import se.sundsvall.partyassets.api.model.AssetAttachmentUpdateRequest;
import se.sundsvall.partyassets.service.AssetAttachmentContent;
import se.sundsvall.partyassets.service.AssetAttachmentService;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class AssetAttachmentResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ASSET_ID = randomUUID().toString();
	private static final String ATTACHMENT_ID = randomUUID().toString();
	private static final String PATH = "/" + MUNICIPALITY_ID + "/assets/" + ASSET_ID + "/attachments";

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

	@Test
	void createAttachment() {
		final var body = new MultipartBodyBuilder();
		body.part("attachment", file()).contentType(APPLICATION_PDF);
		body.part("category", "LOKALRITNING");
		body.part("description", "Ritning");

		when(serviceMock.createAttachment(eq(MUNICIPALITY_ID), eq(ASSET_ID), any(MultipartFile.class), eq("LOKALRITNING"), eq("Ritning"))).thenReturn(ATTACHMENT_ID);

		webTestClient.post()
			.uri(PATH)
			.contentType(MULTIPART_FORM_DATA)
			.bodyValue(body.build())
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().location(PATH + "/" + ATTACHMENT_ID);

		verify(serviceMock).createAttachment(eq(MUNICIPALITY_ID), eq(ASSET_ID), any(MultipartFile.class), eq("LOKALRITNING"), eq("Ritning"));
	}

	@Test
	void createAttachmentWithoutOptionalParts() {
		final var body = new MultipartBodyBuilder();
		body.part("attachment", file()).contentType(APPLICATION_PDF);

		when(serviceMock.createAttachment(eq(MUNICIPALITY_ID), eq(ASSET_ID), any(MultipartFile.class), eq(null), eq(null))).thenReturn(ATTACHMENT_ID);

		webTestClient.post()
			.uri(PATH)
			.contentType(MULTIPART_FORM_DATA)
			.bodyValue(body.build())
			.exchange()
			.expectStatus().isCreated();

		verify(serviceMock).createAttachment(eq(MUNICIPALITY_ID), eq(ASSET_ID), any(MultipartFile.class), eq(null), eq(null));
	}

	@Test
	void readAttachments() {
		when(serviceMock.readAttachments(MUNICIPALITY_ID, ASSET_ID)).thenReturn(List.of(AssetAttachment.create().withId(ATTACHMENT_ID)));

		final var response = webTestClient.get()
			.uri(PATH)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(AssetAttachment.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).hasSize(1);
		assertThat(response.getFirst().getId()).isEqualTo(ATTACHMENT_ID);
		verify(serviceMock).readAttachments(MUNICIPALITY_ID, ASSET_ID);
	}

	@Test
	void readAttachment() {
		when(serviceMock.readAttachment(MUNICIPALITY_ID, ASSET_ID, ATTACHMENT_ID))
			.thenReturn(new AssetAttachmentContent("lokalritning.pdf", APPLICATION_PDF_VALUE, "content".getBytes()));

		final var response = webTestClient.get()
			.uri(PATH + "/" + ATTACHMENT_ID)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().valueEquals(CONTENT_TYPE, APPLICATION_PDF_VALUE)
			.expectHeader().value(CONTENT_DISPOSITION, disposition -> assertThat(disposition).startsWith("attachment; filename=\"lokalritning.pdf\""))
			.expectBody(byte[].class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo("content".getBytes());
		verify(serviceMock).readAttachment(MUNICIPALITY_ID, ASSET_ID, ATTACHMENT_ID);
	}

	@Test
	void updateAttachment() {
		final var request = AssetAttachmentUpdateRequest.create().withCategory("LOKALRITNING");
		when(serviceMock.updateAttachment(MUNICIPALITY_ID, ASSET_ID, ATTACHMENT_ID, request)).thenReturn(AssetAttachment.create().withId(ATTACHMENT_ID).withCategory("LOKALRITNING"));

		final var response = webTestClient.patch()
			.uri(PATH + "/" + ATTACHMENT_ID)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectBody(AssetAttachment.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getCategory()).isEqualTo("LOKALRITNING");
		verify(serviceMock).updateAttachment(MUNICIPALITY_ID, ASSET_ID, ATTACHMENT_ID, request);
	}

	@Test
	void deleteAttachment() {
		webTestClient.delete()
			.uri(PATH + "/" + ATTACHMENT_ID)
			.exchange()
			.expectStatus().isNoContent();

		verify(serviceMock).deleteAttachment(MUNICIPALITY_ID, ASSET_ID, ATTACHMENT_ID);
	}
}
