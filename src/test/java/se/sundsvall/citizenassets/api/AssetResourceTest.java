package se.sundsvall.citizenassets.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;

import se.sundsvall.citizenassets.Application;
import se.sundsvall.citizenassets.TestFactory;
import se.sundsvall.citizenassets.api.model.Asset;
import se.sundsvall.citizenassets.api.model.AssetSearchRequest;
import se.sundsvall.citizenassets.api.model.Status;
import se.sundsvall.citizenassets.service.AssetService;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class AssetResourceTest {

	@MockBean
	private AssetService assetServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getAssets() {
		final var assets = List.of(TestFactory.getAsset());
		when(assetServiceMock.getAssets(any(AssetSearchRequest.class))).thenReturn(assets);
		final var result = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/assets")
				.queryParam("partyId", UUID.randomUUID())
				.queryParam("assetId", "assetId")
				.queryParam("status", Status.ACTIVE)
				.queryParam("type", "PERMIT")
				.queryParam("issued", "2020-01-01")
				.queryParam("validTo", "2020-01-01")
				.queryParam("description", "description")
				.queryParam("caseReferenceIds", UUID.randomUUID())
				.build())
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentType(APPLICATION_JSON)
			.expectBodyList(Asset.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).usingRecursiveComparison().isEqualTo(assets);

		verify(assetServiceMock).getAssets(any(AssetSearchRequest.class));
		verifyNoMoreInteractions(assetServiceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = { "imNotARealUUID", "1", "1234-1234-1234-1234" })
	void getAssets_faultyPartyId(String uuid) {

		final var test = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/assets")
				.queryParam("partyId", uuid)
				.build())
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(test).isNotNull();
		assertThat(test.getStatus()).isNotNull();
		assertThat(test.getStatus().getStatusCode()).isEqualTo(400);
		assertThat(test.getViolations().get(0).getMessage()).isEqualTo("not a valid UUID");
		assertThat(test.getViolations().get(0).getField()).isEqualTo("partyId");
		assertThat(test.getTitle()).isEqualTo("Constraint Violation");
		assertThat(test.getType()).isEqualTo(ConstraintViolationProblem.TYPE);
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createAsset(@Value("${local.server.port}") int serverPort) {

		final var uuid = UUID.randomUUID().toString();
		final var assetRequest = TestFactory.getAssetCreateRequest(UUID.randomUUID().toString());

		when(assetServiceMock.createAsset(assetRequest)).thenReturn(uuid.toString());

		webTestClient.post()
			.uri("/assets")
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus()
			.isCreated()
			.expectHeader()
			.location("http://localhost:" + serverPort + "/assets/" + uuid);

		verify(assetServiceMock).createAsset(assetRequest);
		verifyNoMoreInteractions(assetServiceMock);
	}

	@Test
	void testUpdateAsset() {
		final var id = UUID.randomUUID().toString();
		final var assetRequest = TestFactory.getAssetUpdateRequest();

		webTestClient.put()
			.uri("/assets/{id}", id)
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus()
			.isNoContent();

		verify(assetServiceMock).updateAsset(id, assetRequest);
		verifyNoMoreInteractions(assetServiceMock);
	}

	@Test
	void testUpdateAsset_faultyUUID() {
		final var id = "imNotARealUUID";
		final var assetRequest = TestFactory.getAssetUpdateRequest();
		final var test = webTestClient.put()
			.uri("/assets/{id}", id)
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(test).isNotNull();
		assertThat(test.getStatus()).isNotNull();
		assertThat(test.getStatus().getStatusCode()).isEqualTo(400);
		assertThat(test.getViolations().get(0).getMessage()).isEqualTo("not a valid UUID");
		assertThat(test.getViolations().get(0).getField()).isEqualTo("updateAsset.id");
		assertThat(test.getTitle()).isEqualTo("Constraint Violation");
		assertThat(test.getType()).isEqualTo(ConstraintViolationProblem.TYPE);
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void testDeleteAsset() {
		final var uuid = UUID.randomUUID().toString();
		webTestClient.delete()
			.uri("/assets/{id}", uuid)
			.exchange()
			.expectStatus()
			.isNoContent();

		verify(assetServiceMock).deleteAsset(uuid);
		verifyNoMoreInteractions(assetServiceMock);
	}

	@Test
	void testDeleteAssert_faultyUUID() {
		final var uuid = "imNotARealUUID";
		final var test = webTestClient.delete()
			.uri("/assets/{id}", uuid)
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(test).isNotNull();
		assertThat(test.getStatus()).isNotNull();
		assertThat(test.getStatus().getStatusCode()).isEqualTo(400);
		assertThat(test.getViolations().get(0).getMessage()).isEqualTo("not a valid UUID");
		assertThat(test.getViolations().get(0).getField()).isEqualTo("deleteAsset.id");
		assertThat(test.getTitle()).isEqualTo("Constraint Violation");
		assertThat(test.getType()).isEqualTo(ConstraintViolationProblem.TYPE);
		verifyNoInteractions(assetServiceMock);
	}
}
