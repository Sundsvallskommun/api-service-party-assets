package se.sundsvall.partyassets.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.service.AssetEventService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class AssetEventResourceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@MockitoBean
	private AssetEventService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void republish() {
		// Act
		webTestClient.post()
			.uri("/{municipalityId}/asset-events/republish", MUNICIPALITY_ID)
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		// Assert
		verify(serviceMock).republish(MUNICIPALITY_ID);
		verifyNoMoreInteractions(serviceMock);
	}
}
