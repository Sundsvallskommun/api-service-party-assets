package se.sundsvall.partyassets.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.TestFactory;
import se.sundsvall.partyassets.api.model.Asset;
import se.sundsvall.partyassets.api.model.AssetCreateRequest;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.service.AssetService;
import se.sundsvall.partyassets.service.JsonSchemaValidationService;
import se.sundsvall.partyassets.service.StatusService;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class AssetResourceTest {

	private static final Map<Status, List<String>> VALID_STATUS_REASONS_FOR_STATUSES = Map.of(
		Status.BLOCKED, List.of("IRREGULARITY", "LOST"),
		Status.ACTIVE, List.of("IRREGULARITY", "LOST"));

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = MUNICIPALITY_ID + "/assets";
	private static final String INVALID = "#invalid#";

	@MockitoBean
	private AssetService assetServiceMock;

	@MockitoBean
	private JsonSchemaValidationService jsonSchemaValidationServiceMock; // Used by Json validation

	@MockitoBean
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
				.queryParam("partyId", randomUUID())
				.queryParam("assetId", "assetId")
				.queryParam("status", Status.ACTIVE)
				.queryParam("type", "PERMIT")
				.queryParam("issued", "2020-01-01")
				.queryParam("validTo", "2020-01-01")
				.queryParam("description", "description")
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
	@ValueSource(strings = {
		"imNotARealUUID", "1", "1234-1234-1234-1234"
	})
	void getAssetsFaultyPartyId(final String uuid) {
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

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/" + INVALID + "/assets")
				.queryParam("partyId", randomUUID())
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
	void getAsset() {
		// Arrange
		final var id = randomUUID().toString();
		final var asset = TestFactory.getAsset();

		when(assetServiceMock.getAsset(MUNICIPALITY_ID, id)).thenReturn(asset);

		// Act
		final var result = webTestClient.get()
			.uri(PATH + "/{id}", id)
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentType(APPLICATION_JSON)
			.expectBody(Asset.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(result).usingRecursiveComparison().isEqualTo(asset);
		verify(assetServiceMock).getAsset(MUNICIPALITY_ID, id);
		verifyNoMoreInteractions(assetServiceMock);
	}

	@Test
	void getAssetFaultyUUID() {

		// Arrange
		final var id = "imNotARealUUID";

		// Act
		final var response = webTestClient.get()
			.uri(PATH + "/{id}", id)
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
		assertThat(response.getViolations().getFirst().getField()).isEqualTo("getAsset.id");
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getType()).isEqualTo(ConstraintViolationProblem.TYPE);

		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void getAssetInvalidMunicipalityId() {

		// Arrange
		final var uuid = randomUUID().toString();

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/" + INVALID + "/assets/" + uuid)
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
			.containsExactly(tuple("getAsset.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createAsset() {

		// Arrange
		final var uuid = randomUUID().toString();
		final var assetRequest = TestFactory.getAssetCreateRequest(randomUUID().toString()).withStatusReason(null);

		when(assetServiceMock.createAsset(MUNICIPALITY_ID, assetRequest)).thenReturn(uuid);
		doNothing().when(jsonSchemaValidationServiceMock).validate(anyString(), anyString(), any(JsonNode.class));

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
	void createAssetEmptyRequest() {

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
				tuple("issued", "must not be null"),
				tuple("partyId", "not a valid UUID"),
				tuple("status", "must not be null"),
				tuple("type", "must not be empty"));
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getType()).isEqualTo(ConstraintViolationProblem.TYPE);
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createAssetInvalidJsonParameter() {

		// Arrange
		final var assetRequest = TestFactory.getAssetCreateRequest(randomUUID().toString()).withStatusReason(null);

		doThrow(Problem.valueOf(BAD_REQUEST, "some-error")).when(jsonSchemaValidationServiceMock).validate(anyString(), anyString(), any(JsonNode.class));

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
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("jsonParameters[0]", "Bad Request: some-error"));
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createAssetFaultyStatusReason() {

		// Arrange
		final var assetRequest = TestFactory.getAssetCreateRequest(randomUUID().toString());

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
		final var assetRequest = TestFactory.getAssetCreateRequest(randomUUID().toString()).withStatusReason(null);

		doNothing().when(jsonSchemaValidationServiceMock).validate(anyString(), anyString(), any(JsonNode.class));

		// Act
		final var response = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path("/" + INVALID + "/assets")
				.queryParam("partyId", randomUUID())
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
	}

	@Test
	void updateAsset() {

		// Arrange
		final var id = randomUUID().toString();
		final var assetRequest = TestFactory.getAssetUpdateRequest().withStatusReason("LOST");

		when(statusServiceMock.getReasonsForAllStatuses(MUNICIPALITY_ID)).thenReturn(VALID_STATUS_REASONS_FOR_STATUSES);
		doNothing().when(jsonSchemaValidationServiceMock).validate(anyString(), anyString(), any(JsonNode.class));

		// Act
		webTestClient.patch()
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
	void updateAssetFaultyStatusReason() {

		// Arrange
		final var id = randomUUID().toString();
		final var assetRequest = TestFactory.getAssetUpdateRequest();

		when(statusServiceMock.getReasonsForAllStatuses(MUNICIPALITY_ID)).thenReturn(VALID_STATUS_REASONS_FOR_STATUSES);

		// Act
		final var response = webTestClient.patch()
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
	void updateAssetFaultyUUID() {

		// Arrange
		final var id = "imNotARealUUID";
		final var assetRequest = TestFactory.getAssetUpdateRequest().withStatusReason("IRREGULARITY");

		when(statusServiceMock.getReasonsForAllStatuses(MUNICIPALITY_ID)).thenReturn(VALID_STATUS_REASONS_FOR_STATUSES);
		doNothing().when(jsonSchemaValidationServiceMock).validate(anyString(), anyString(), any(JsonNode.class));

		// Act
		final var response = webTestClient.patch()
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
		final var uuid = randomUUID().toString();
		final var assetRequest = TestFactory.getAssetUpdateRequest().withStatusReason(null);

		doNothing().when(jsonSchemaValidationServiceMock).validate(anyString(), anyString(), any(JsonNode.class));

		// Act
		final var response = webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path("/" + INVALID + "/assets/" + uuid)
				.queryParam("partyId", randomUUID())
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
	}

	@Test
	void updateAssetInvalidJsonParameter() {

		// Arrange
		final var uuid = randomUUID().toString();
		final var assetRequest = TestFactory.getAssetUpdateRequest().withStatusReason(null);

		doThrow(Problem.valueOf(BAD_REQUEST, "some-error")).when(jsonSchemaValidationServiceMock).validate(anyString(), anyString(), any(JsonNode.class));

		// Act
		final var response = webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path("/" + MUNICIPALITY_ID + "/assets/" + uuid)
				.queryParam("partyId", randomUUID())
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
			.containsExactly(tuple("jsonParameters[0]", "Bad Request: some-error"));

		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void deleteAsset() {

		// Arrange
		final var uuid = randomUUID().toString();

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
	void deleteAssertFaultyUUID() {

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
		final var uuid = randomUUID().toString();

		// Act
		final var response = webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path("/" + INVALID + "/assets/" + uuid)
				.queryParam("partyId", randomUUID())
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
