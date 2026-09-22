package se.sundsvall.partyassets.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.service.AssetRevisionService;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class AssetRevisionResourceFailureTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ASSET_ID = randomUUID().toString();
	private static final String INVALID_MUNICIPALITY_ID = "invalid-municipality-id";
	private static final String INVALID_UUID = "not-a-valid-uuid";

	@MockitoBean
	private AssetRevisionService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	private static String path(final String municipalityId, final String assetId) {
		return "/" + municipalityId + "/assets/" + assetId + "/revisions";
	}

	@Test
	void getRevisionsWithInvalidMunicipalityId() {
		final var response = webTestClient.get().uri(path(INVALID_MUNICIPALITY_ID, ASSET_ID))
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getRevisions.municipalityId", "not a valid municipality ID"));
		verifyNoInteractions(serviceMock);
	}

	@Test
	void getRevisionsWithInvalidAssetId() {
		final var response = webTestClient.get().uri(path(MUNICIPALITY_ID, INVALID_UUID))
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getRevisions.id", "not a valid UUID"));
		verifyNoInteractions(serviceMock);
	}

	@Test
	void getRevisionWithInvalidMunicipalityId() {
		final var response = webTestClient.get().uri(path(INVALID_MUNICIPALITY_ID, ASSET_ID) + "/1")
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getRevision.municipalityId", "not a valid municipality ID"));
		verifyNoInteractions(serviceMock);
	}

	@Test
	void getRevisionWithANegativeRevisionNumber() {
		final var response = webTestClient.get().uri(path(MUNICIPALITY_ID, ASSET_ID) + "/-1")
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getRevision.revision", "must be greater than or equal to 0"));
		verifyNoInteractions(serviceMock);
	}
}
