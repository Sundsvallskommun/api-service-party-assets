package se.sundsvall.partyassets.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.TestFactory;
import se.sundsvall.partyassets.api.model.Asset;
import se.sundsvall.partyassets.api.model.AssetCreateRequest;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.service.AssetService;
import se.sundsvall.partyassets.service.StatusService;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class AssetResourceTest {

	private static final Map<Status, List<String>> VALID_STATUS_REASONS_FOR_STATUSES = Map.of(
		Status.BLOCKED, List.of("IRREGULARITY", "LOST"),
		Status.ACTIVE, List.of("IRREGULARITY", "LOST"));

	private static final String MUNICIPALITY_ID = "2281";

	private static final String PATH = MUNICIPALITY_ID + "/assets";

	private static final String INVALID = "#invalid#";

	@MockBean
	private AssetService assetServiceMock;

	@MockBean
	private StatusService statusServiceMock; // Used by status reason validators

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getAssets() {
		// Arrange
		final var assets = List.of(TestFactory.getAsset());

		when(assetServiceMock.getAssets(eq(MUNICIPALITY_ID), any(AssetSearchRequest.class))).thenReturn(assets);

		// Act
		final var result = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(PATH)
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

		// Assert
		assertThat(result).usingRecursiveComparison().isEqualTo(assets);
		verify(assetServiceMock).getAssets(eq(MUNICIPALITY_ID), any(AssetSearchRequest.class));
		verifyNoMoreInteractions(assetServiceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {"imNotARealUUID", "1", "1234-1234-1234-1234"})
	void getAssets_faultyPartyId(final String uuid) {
		// Act
		final var test = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(PATH)
				.queryParam("partyId", uuid)
				.build())
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(test).isNotNull();
		assertThat(test.getStatus()).isNotNull();
		assertThat(test.getStatus().getStatusCode()).isEqualTo(400);
		assertThat(test.getViolations().getFirst().getMessage()).isEqualTo("not a valid UUID");
		assertThat(test.getViolations().getFirst().getField()).isEqualTo("partyId");
		assertThat(test.getTitle()).isEqualTo("Constraint Violation");
		assertThat(test.getType()).isEqualTo(ConstraintViolationProblem.TYPE);
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void getAssetsInvalidMunicipalityId() {
		// Arrange

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/" + INVALID + "/assets")
				.queryParam("partyId", UUID.randomUUID())
				.build())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getAssets.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createAsset() {
		// Arrange
		final var uuid = UUID.randomUUID().toString();
		final var assetRequest = TestFactory.getAssetCreateRequest(UUID.randomUUID().toString()).withStatusReason(null);

		when(assetServiceMock.createAsset(MUNICIPALITY_ID, assetRequest)).thenReturn(uuid);

		// Act
		webTestClient.post()
			.uri(PATH)
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus()
			.isCreated()
			.expectHeader().location("/" + MUNICIPALITY_ID + "/assets/" + uuid);

		// Assert
		verify(assetServiceMock).createAsset(MUNICIPALITY_ID, assetRequest);
		verifyNoMoreInteractions(assetServiceMock);
	}

	@Test
	void createAsset_emptyRequest() {
		// Act
		final var response = webTestClient.post()
			.uri(PATH)
			.bodyValue(AssetCreateRequest.create())
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isNotNull();
		assertThat(response.getStatus().getStatusCode()).isEqualTo(400);
		assertThat(response.getViolations())
			.extracting(
				Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(
				tuple("assetId", "must not be empty"),
				tuple("issued", "must not be null"),
				tuple("partyId", "not a valid UUID"),
				tuple("status", "must not be null"),
				tuple("type", "must not be empty"));
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getType()).isEqualTo(ConstraintViolationProblem.TYPE);
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createAsset_faultyStatusReason() {
		// Arrange
		final var assetRequest = TestFactory.getAssetCreateRequest(UUID.randomUUID().toString());

		// Act
		final var response = webTestClient.post()
			.uri(PATH)
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isNotNull();
		assertThat(response.getStatus().getStatusCode()).isEqualTo(400);
		assertThat(response.getViolations().getFirst().getMessage()).isEqualTo("'statusReason' is not valid reason for status ACTIVE. Valid reasons are [].");
		assertThat(response.getViolations().getFirst().getField()).isEqualTo("assetCreateRequest");
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getType()).isEqualTo(ConstraintViolationProblem.TYPE);
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createAssetInvalidMunicipalityId() {
		// Arrange
		final var assetRequest = TestFactory.getAssetCreateRequest(UUID.randomUUID().toString()).withStatusReason(null);
		// Act
		final var response = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path("/" + INVALID + "/assets")
				.queryParam("partyId", UUID.randomUUID())
				.build())
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("createAsset.municipalityId", "not a valid municipality ID"));
		verifyNoInteractions(assetServiceMock);

		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void testUpdateAsset() {
		// Arrange
		final var id = UUID.randomUUID().toString();
		final var assetRequest = TestFactory.getAssetUpdateRequest().withStatusReason("LOST");

		when(statusServiceMock.getReasonsForAllStatuses(MUNICIPALITY_ID)).thenReturn(VALID_STATUS_REASONS_FOR_STATUSES);

		// Act
		webTestClient.put()
			.uri(PATH + "/{id}", id)
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus()
			.isNoContent();

		// Assert
		verify(assetServiceMock).updateAsset(MUNICIPALITY_ID, id, assetRequest);
		verifyNoMoreInteractions(assetServiceMock);
	}

	@Test
	void testUpdateAsset_faultyStatusReason() {
		// Arrange
		final var id = UUID.randomUUID().toString();
		final var assetRequest = TestFactory.getAssetUpdateRequest();

		when(statusServiceMock.getReasonsForAllStatuses(MUNICIPALITY_ID)).thenReturn(VALID_STATUS_REASONS_FOR_STATUSES);

		// Act
		final var response = webTestClient.put()
			.uri(PATH + "/{id}", id)
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isNotNull();
		assertThat(response.getStatus().getStatusCode()).isEqualTo(400);
		assertThat(response.getViolations().getFirst().getMessage()).isEqualTo("'statusReasonUpdated' is not valid reason for status BLOCKED. Valid reasons are [IRREGULARITY, LOST].");
		assertThat(response.getViolations().getFirst().getField()).isEqualTo("assetUpdateRequest");
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getType()).isEqualTo(ConstraintViolationProblem.TYPE);
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void testUpdateAsset_faultyUUID() {
		// Arrange
		final var id = "imNotARealUUID";
		final var assetRequest = TestFactory.getAssetUpdateRequest().withStatusReason("IRREGULARITY");

		when(statusServiceMock.getReasonsForAllStatuses(MUNICIPALITY_ID)).thenReturn(VALID_STATUS_REASONS_FOR_STATUSES);

		// Act
		final var response = webTestClient.put()
			.uri(PATH + "/{id}", id)
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isNotNull();
		assertThat(response.getStatus().getStatusCode()).isEqualTo(400);
		assertThat(response.getViolations().getFirst().getMessage()).isEqualTo("not a valid UUID");
		assertThat(response.getViolations().getFirst().getField()).isEqualTo("updateAsset.id");
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getType()).isEqualTo(ConstraintViolationProblem.TYPE);
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void updateAssetInvalidMunicipalityId() {
		// Arrange
		final var uuid = UUID.randomUUID().toString();
		final var assetRequest = TestFactory.getAssetUpdateRequest().withStatusReason(null);

		// Act
		final var response = webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path("/" + INVALID + "/assets/" + uuid)
				.queryParam("partyId", UUID.randomUUID())
				.build())
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("updateAsset.municipalityId", "not a valid municipality ID"));
		verifyNoInteractions(assetServiceMock);

		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void testDeleteAsset() {
		// Arrange
		final var uuid = UUID.randomUUID().toString();

		// Act
		webTestClient.delete()
			.uri(PATH + "/{id}", uuid)
			.exchange()
			.expectStatus()
			.isNoContent();

		// Assert
		verify(assetServiceMock).deleteAsset(MUNICIPALITY_ID, uuid);
		verifyNoMoreInteractions(assetServiceMock);
	}

	@Test
	void testDeleteAssert_faultyUUID() {
		// Arrange
		final var uuid = "imNotARealUUID";

		// Act
		final var response = webTestClient.delete()
			.uri(PATH + "/{id}", uuid)
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isNotNull();
		assertThat(response.getStatus().getStatusCode()).isEqualTo(400);
		assertThat(response.getViolations().getFirst().getMessage()).isEqualTo("not a valid UUID");
		assertThat(response.getViolations().getFirst().getField()).isEqualTo("deleteAsset.id");
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getType()).isEqualTo(ConstraintViolationProblem.TYPE);
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void deleteAssetInvalidMunicipalityId() {
		// Arrange
		final var uuid = UUID.randomUUID().toString();

		// Act
		final var response = webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path("/" + INVALID + "/assets/" + uuid)
				.queryParam("partyId", UUID.randomUUID())
				.build())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("deleteAsset.municipalityId", "not a valid municipality ID"));
		verifyNoInteractions(assetServiceMock);
	}

}
