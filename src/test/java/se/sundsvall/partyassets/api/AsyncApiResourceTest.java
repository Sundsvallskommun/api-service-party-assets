package se.sundsvall.partyassets.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.integration.events.AssetEventsAsyncApi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class AsyncApiResourceTest {

	// The ö makes the test fail if the body loses its encoding on the way out
	private static final String DOCUMENT = "info:\n  title: Bygglov för fritidshus\n";

	@MockitoBean
	private AssetEventsAsyncApi asyncApiMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void asyncApi() {
		// Arrange
		when(asyncApiMock.yaml()).thenReturn(DOCUMENT);

		// Act
		final var response = webTestClient.get()
			.uri("/api-docs/asyncapi.yaml")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith("application/yaml")
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isEqualTo(DOCUMENT);
		verify(asyncApiMock).yaml();
		verifyNoMoreInteractions(asyncApiMock);
	}
}
