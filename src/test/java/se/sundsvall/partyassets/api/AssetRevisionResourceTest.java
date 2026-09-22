package se.sundsvall.partyassets.api;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.api.model.AssetRevision;
import se.sundsvall.partyassets.service.AssetRevisionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class AssetRevisionResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ASSET_ID = "e84b72ee-1a34-44b5-b8f6-2e0e42e99010";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/assets/" + ASSET_ID + "/revisions";

	@MockitoBean
	private AssetRevisionService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getRevisions() {
		final var revisions = List.of(
			AssetRevision.create().withId(ASSET_ID).withRevision(1).withActor("joe01doe"),
			AssetRevision.create().withId(ASSET_ID).withRevision(0));
		when(serviceMock.getRevisions(MUNICIPALITY_ID, ASSET_ID)).thenReturn(revisions);

		final var response = webTestClient.get().uri(PATH)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(AssetRevision.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(revisions);
		verify(serviceMock).getRevisions(MUNICIPALITY_ID, ASSET_ID);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getRevision() {
		final var revision = AssetRevision.create().withId(ASSET_ID).withRevision(2).withActor("joe01doe");
		when(serviceMock.getRevision(MUNICIPALITY_ID, ASSET_ID, 2)).thenReturn(revision);

		final var response = webTestClient.get().uri(PATH + "/2")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(AssetRevision.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(revision);
		verify(serviceMock).getRevision(MUNICIPALITY_ID, ASSET_ID, 2);
		verifyNoMoreInteractions(serviceMock);
	}

	// Numbering starts at 0, so revision 0 has to be reachable rather than rejected as a missing value.
	@Test
	void getRevisionZero() {
		when(serviceMock.getRevision(MUNICIPALITY_ID, ASSET_ID, 0)).thenReturn(AssetRevision.create().withRevision(0));

		webTestClient.get().uri(PATH + "/0")
			.exchange()
			.expectStatus().isOk();

		verify(serviceMock).getRevision(MUNICIPALITY_ID, ASSET_ID, 0);
	}

	@Test
	void getRevisionsForAnAssetWithoutHistory() {
		when(serviceMock.getRevisions(MUNICIPALITY_ID, UUID.fromString(ASSET_ID).toString())).thenReturn(List.of());

		webTestClient.get().uri(PATH)
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(AssetRevision.class)
			.hasSize(0);
	}
}
