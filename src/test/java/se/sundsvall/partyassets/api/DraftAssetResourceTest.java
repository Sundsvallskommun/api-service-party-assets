package se.sundsvall.partyassets.api;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.TestFactory;
import se.sundsvall.partyassets.api.model.Asset;
import se.sundsvall.partyassets.api.model.AssetCreateRequest;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.service.AssetService;
import se.sundsvall.partyassets.service.JsonSchemaValidationService;
import se.sundsvall.partyassets.service.StatusService;
import tools.jackson.databind.JsonNode;

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
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
class DraftAssetResourceTest {

	private static final Map<Status, List<String>> VALID_STATUS_REASONS_FOR_STATUSES = Map.of(
		Status.BLOCKED, List.of("IRREGULARITY", "LOST"),
		Status.ACTIVE, List.of("IRREGULARITY", "LOST"));

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = MUNICIPALITY_ID + "/asset-drafts";
	private static final String INVALID = "#invalid#";

	@Captor
	private ArgumentCaptor<AssetCreateRequest> createRequestCaptor;

	@MockitoBean
	private AssetService assetServiceMock;

	@MockitoBean
	private JsonSchemaValidationService jsonSchemaValidationServiceMock; // Used by Json validation

	@MockitoBean
	private StatusService statusServiceMock; // Used by status reason validators

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getDraftAssets() {
		// Arrange
		final var assets = List.of(TestFactory.getDraftAsset());

		when(assetServiceMock.getDraftAssets(eq(MUNICIPALITY_ID), any(AssetSearchRequest.class))).thenReturn(assets);

		// Act
		final var result = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(PATH)
				.queryParam("partyId", randomUUID())
				.queryParam("assetId", "assetId")
				.queryParam("status", Status.DRAFT)
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
		verify(assetServiceMock).getDraftAssets(eq(MUNICIPALITY_ID), any(AssetSearchRequest.class));
		verifyNoMoreInteractions(assetServiceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"imNotARealUUID", "1", "1234-1234-1234-1234"
	})
	void getDraftAssetsFaultyPartyId(final String uuid) {
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
		assertThat(test.getStatus().value()).isEqualTo(400);
		assertThat(test.getViolations().getFirst().message()).isEqualTo("not a valid UUID");
		assertThat(test.getViolations().getFirst().field()).isEqualTo("partyId");
		assertThat(test.getTitle()).isEqualTo("Constraint Violation");
		assertThat(test.getType()).isEqualTo(ConstraintViolationProblem.TYPE);
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void getDraftAssetsInvalidMunicipalityId() {
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
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getAssets.municipalityId", "not a valid municipality ID"));
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createDraftAsset() {
		// Arrange
		final var uuid = randomUUID().toString();
		final var assetRequest = TestFactory.getAssetCreateRequest(randomUUID().toString()).withStatus(Status.DRAFT).withStatusReason(null);

		when(assetServiceMock.createAsset(MUNICIPALITY_ID, assetRequest)).thenReturn(uuid);
		doNothing().when(jsonSchemaValidationServiceMock).validate(anyString(), anyString(), any(JsonNode.class));

		// Act
		webTestClient.post()
			.uri(PATH)
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus()
			.isCreated()
			.expectHeader().location("/" + MUNICIPALITY_ID + "/asset-drafts/" + uuid);

		// Assert
		verify(assetServiceMock).createAsset(eq(MUNICIPALITY_ID), createRequestCaptor.capture());
		verifyNoMoreInteractions(assetServiceMock);

		var createRequest = createRequestCaptor.getValue();
		assertThat(createRequest.getStatus()).isEqualTo(Status.DRAFT);
	}

	@ParameterizedTest
	@EnumSource(value = Status.class, names = "DRAFT", mode = EnumSource.Mode.EXCLUDE)
	void createAssetWithOtherThanDraftStatus(final Status status) {
		// Arrange
		final var assetRequest = TestFactory.getAssetCreateRequest(randomUUID().toString()).withStatus(status).withStatusReason(null);
		final var expectedJsonMessage = """
			{
				"detail": "Status %s is not allowed when creating draft assets",
				"status" : 400,
				"title" : "Bad Request"
			}""".formatted(status);

		// Act
		webTestClient.post()
			.uri(PATH)
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody()
			.json(expectedJsonMessage);

		// Assert
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createDraftAssetEmptyRequest() {
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
		assertThat(response.getStatus().value()).isEqualTo(400);
		assertThat(response.getViolations())
			.extracting(
				Violation::field, Violation::message)
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
	void createDraftAssetInvalidJsonParameter() {
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
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("jsonParameters[0]", "Bad Request: some-error"));
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createDraftAssetFaultyStatusReason() {
		// Arrange
		final var assetRequest = TestFactory.getAssetCreateRequest(randomUUID().toString());
		final var expectedJsonMessage = """
			{
				"type" : "https://github.com/Sundsvallskommun/dept44/problems/constraint-violation",
				"status" : 400,
				"violations" : [ {
					"field" : "assetCreateRequest",
					"message" : "'statusReason' is not valid reason for status ACTIVE. Valid reasons are []."
				} ],
				"title" : "Constraint Violation"
			}""";

		// Act
		webTestClient.post()
			.uri(PATH)
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody()
			.json(expectedJsonMessage);

		// Assert
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createDraftAssetInvalidMunicipalityId() {
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
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("createAsset.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void updateDraftAsset() {

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
	void updateDraftAssetFaultyStatusReason() {
		// Arrange
		final var id = randomUUID().toString();
		final var assetRequest = TestFactory.getAssetUpdateRequest();
		final var expectedJsonMessage = """
			{
				"type" : "https://github.com/Sundsvallskommun/dept44/problems/constraint-violation",
				"status" : 400,
				"violations" : [ {
					"field" : "assetUpdateRequest",
					"message" : "'statusReasonUpdated' is not valid reason for status BLOCKED. Valid reasons are [IRREGULARITY, LOST]."
				} ],
				"title" : "Constraint Violation"
			}""";

		when(statusServiceMock.getReasonsForAllStatuses(MUNICIPALITY_ID)).thenReturn(VALID_STATUS_REASONS_FOR_STATUSES);

		// Act
		webTestClient.patch()
			.uri(PATH + "/{id}", id)
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody()
			.json(expectedJsonMessage);

		// Assert
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void updateDraftAssetFaultyUUID() {
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
		assertThat(response.getStatus().value()).isEqualTo(400);
		assertThat(response.getViolations().getFirst().message()).isEqualTo("not a valid UUID");
		assertThat(response.getViolations().getFirst().field()).isEqualTo("updateDraftAsset.id");
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getType()).isEqualTo(ConstraintViolationProblem.TYPE);

		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void updateDraftAssetInvalidMunicipalityId() {
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
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("updateAsset.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void updateDraftAssetInvalidJsonParameter() {
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
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("jsonParameters[0]", "Bad Request: some-error"));

		verifyNoInteractions(assetServiceMock);
	}
}
