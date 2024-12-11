package se.sundsvall.partyassets.pr3import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

import generated.se.sundsvall.messaging.EmailRequest;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import se.sundsvall.partyassets.Application;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class PR3ImportResourceTest {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String PATH = "/" + MUNICIPALITY_ID + "/pr3import";

	@MockitoBean
	private PR3Importer mockImporter;

	@MockitoBean
	private PR3ImportMessagingClient mockMessagingClient;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void handleImport() throws IOException {
		final var importFile = new ClassPathResource("/test.xlsx");
		final var importResult = new PR3Importer.Result()
			.withTotal(12)
			.withFailed(2)
			.withFailedExcelData(importFile.getContentAsByteArray());
		when(mockImporter.importFromExcel(any(InputStream.class), any(String.class))).thenReturn(importResult);

		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("file", importFile);
		multipartBodyBuilder.part("email", "someone@something.com");

		final var result = webTestClient.post()
			.uri(PATH)
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentType(APPLICATION_JSON)
			.expectBody(PR3Importer.Result.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull();
		assertThat(result.getTotal()).isEqualTo(importResult.getTotal());
		assertThat(result.getFailed()).isEqualTo(importResult.getFailed());
		assertThat(result.getFailedExcelData()).isNull();

		verify(mockImporter).importFromExcel(any(InputStream.class), any(String.class));
		verify(mockMessagingClient).sendEmail(any(String.class), any(EmailRequest.class));
		verifyNoMoreInteractions(mockImporter);
		verifyNoMoreInteractions(mockMessagingClient);
	}

	@Test
	void handleImportWithInvalidInput() {
		final var response = webTestClient.post()
			.uri(PATH)
			.contentType(MULTIPART_FORM_DATA)
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isNotNull();
		assertThat(response.getStatus().getStatusCode()).isEqualTo(400);
		assertThat(response.getTitle()).isEqualTo("Bad Request");
		assertThat(response.getDetail()).isEqualTo("Failed to parse multipart servlet request");

		verifyNoInteractions(mockImporter);
		verifyNoInteractions(mockMessagingClient);
	}

}
