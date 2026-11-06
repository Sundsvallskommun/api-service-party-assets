package se.sundsvall.partyassets.api;

import com.turkraft.springfilter.converter.FilterSpecification;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.TestFactory;
import se.sundsvall.partyassets.api.model.Asset;
import se.sundsvall.partyassets.api.model.AssetCreateRequest;
import se.sundsvall.partyassets.api.model.AssetPagedResponse;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.service.AssetService;
import se.sundsvall.partyassets.service.JsonSchemaValidationService;
import se.sundsvall.partyassets.service.StatusService;
import se.sundsvall.partyassets.service.VersionedAsset;
import tools.jackson.databind.JsonNode;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.params.provider.Arguments.arguments;
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
import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.Order.desc;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class AssetResourceTest {

	private static final Map<Status, List<String>> VALID_STATUS_REASONS_FOR_STATUSES = Map.of(
		Status.BLOCKED, List.of("IRREGULARITY", "LOST"),
		Status.ACTIVE, List.of("IRREGULARITY", "LOST"));

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = MUNICIPALITY_ID + "/assets";
	private static final String SEARCH_PATH = "/" + MUNICIPALITY_ID + "/assets/search";
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
		assertThat(test.getStatus().value()).isEqualTo(400);
		assertThat(test.getViolations().getFirst().message()).isEqualTo("not a valid UUID");
		assertThat(test.getViolations().getFirst().field()).isEqualTo("partyId");
		assertThat(test.getTitle()).isEqualTo("Constraint Violation");
		assertThat(test.getType()).isEqualTo(ConstraintViolationProblem.TYPE);
		verifyNoInteractions(assetServiceMock);
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
		"en-US | 1/1/27", "sv-SE | 27-01-01"
	})
	void getAssetsWithDateInTheFormatOfTheLanguageOfTheRequest(final String language, final String date) {
		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(PATH)
				.queryParam("partyId", randomUUID())
				.queryParam("issued", "{date}")
				.build(date))
			.header(ACCEPT_LANGUAGE, language)
			.exchange()
			.expectStatus().isBadRequest();

		// Assert
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
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getAssets.municipalityId", "not a valid municipality ID"));
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void searchAssetsWithDefaults() {
		// Arrange
		final var response = AssetPagedResponse.create()
			.withAssets(List.of(TestFactory.getAsset()))
			.withMetaData(PagingAndSortingMetaData.create().withPage(1).withLimit(100).withCount(1).withTotalRecords(1).withTotalPages(1));

		when(assetServiceMock.searchAssets(eq(MUNICIPALITY_ID), any(), any())).thenReturn(response);

		// Act
		final var result = webTestClient.get()
			.uri(SEARCH_PATH)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(AssetPagedResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert
		final ArgumentCaptor<Specification<AssetEntity>> filterCaptor = ArgumentCaptor.captor();
		assertThat(result).usingRecursiveComparison().isEqualTo(response);
		verify(assetServiceMock).searchAssets(eq(MUNICIPALITY_ID), filterCaptor.capture(), eq(PageRequest.of(0, 100, Sort.by(asc("created"), asc("id")))));
		assertThat(filterCaptor.getValue()).isInstanceOfSatisfying(FilterSpecification.class, filter -> assertThat(filter.getFilter()).isNull());
		verifyNoMoreInteractions(assetServiceMock);
	}

	@Test
	void searchAssetsWithFilterPagingAndSorting() {
		// Arrange
		when(assetServiceMock.searchAssets(eq(MUNICIPALITY_ID), any(), any())).thenReturn(AssetPagedResponse.create());

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(SEARCH_PATH)
				.queryParam("filter", "{filter}")
				.queryParam("page", 2)
				.queryParam("limit", 10)
				.queryParam("sortBy", "validTo", "title")
				.queryParam("sortDirection", "DESC")
				.build("type:'AlcoholServingPermit' and status:'ACTIVE'"))
			.exchange()
			.expectStatus().isOk();

		// Assert
		final ArgumentCaptor<Specification<AssetEntity>> filterCaptor = ArgumentCaptor.captor();
		verify(assetServiceMock).searchAssets(eq(MUNICIPALITY_ID), filterCaptor.capture(), eq(PageRequest.of(1, 10, Sort.by(desc("validTo"), desc("title"), desc("id")))));
		assertThat(filterCaptor.getValue()).isInstanceOfSatisfying(FilterSpecification.class, filter -> assertThat(filter.getFilter()).isNotNull());
		verifyNoMoreInteractions(assetServiceMock);
	}

	@ParameterizedTest
	@MethodSource("scalarAssetAttributes")
	void searchAssetsSortedByAssetAttribute(final String attribute) {
		// Arrange
		when(assetServiceMock.searchAssets(eq(MUNICIPALITY_ID), any(), any())).thenReturn(AssetPagedResponse.create());

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(SEARCH_PATH).queryParam("sortBy", attribute).build())
			.exchange()
			.expectStatus().isOk();

		// Assert
		verify(assetServiceMock).searchAssets(eq(MUNICIPALITY_ID), any(), any());
		verifyNoMoreInteractions(assetServiceMock);
	}

	@ParameterizedTest
	@MethodSource("sortByParameters")
	void searchAssetsWithSortByGivenAs(final String parameter, final String value, final Sort expectedSort) {
		// Arrange
		when(assetServiceMock.searchAssets(eq(MUNICIPALITY_ID), any(), any())).thenReturn(AssetPagedResponse.create());

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(SEARCH_PATH).queryParam(parameter, "{value}").build(value))
			.exchange()
			.expectStatus().isOk();

		// Assert
		verify(assetServiceMock).searchAssets(eq(MUNICIPALITY_ID), any(), eq(PageRequest.of(0, 100, expectedSort)));
		verifyNoMoreInteractions(assetServiceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"actor", "revision", "version", "partyType", "municipalityId", "attachments", "additionalParameters", "jsonParameters", "unknown"
	})
	void searchAssetsSortedByAttributeOutsideTheAssetModel(final String attribute) {
		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(SEARCH_PATH).queryParam("sortBy", attribute).build())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getViolations()).singleElement()
			.satisfies(violation -> assertThat(violation.message()).startsWith("One or more of the sortBy properties [%s] are not valid.".formatted(attribute)));
		verifyNoInteractions(assetServiceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"status:", "status:'ACTIVE' and", "description:'it''s'", "(status:'ACTIVE'", "foo(type)", "status : `ACTIVE`"
	})
	void searchAssetsWithInvalidFilterSyntax(final String filter) {
		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(SEARCH_PATH).queryParam("filter", "{filter}").build(filter))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Invalid Filter Content")
			.jsonPath("$.detail").isNotEmpty();

		// Assert
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void searchAssetsWithUnknownFunction() {
		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(SEARCH_PATH).queryParam("filter", "{filter}").build("validTo > now()"))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.title").isEqualTo("Invalid Filter Content")
			.jsonPath("$.detail").isEqualTo("Unrecognized function `now`");

		// Assert
		verifyNoInteractions(assetServiceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"status:'ACTIVE' and actor:'joe01doe'", "exists(actor : 'joe01doe')"
	})
	void searchAssetsWithFilterOnAttributeOutsideTheAssetModel(final String filter) {
		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(SEARCH_PATH).queryParam("filter", "{filter}").build(filter))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getViolations()).singleElement()
			.satisfies(violation -> {
				assertThat(violation.field()).isEqualTo("searchAssets.filter");
				assertThat(violation.message()).startsWith("The attributes [actor] are not filterable. Filterable attributes are [");
			});
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void searchAssetsWithValuesThatAttributesCannotHold() {
		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(SEARCH_PATH).queryParam("filter", "{filter}").build("status:'FOO' and validTo < 'tomorrow' and created > '2025-01-01T00:00:00+01:00' and issued : ''"))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(
				tuple("searchAssets.filter", "'FOO' is not a valid value for status."),
				tuple("searchAssets.filter", "'tomorrow' is not a valid value for validTo."),
				tuple("searchAssets.filter", "'' is not a valid value for issued."));
		verifyNoInteractions(assetServiceMock);
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
		"en-US | 1/1/27", "sv-SE | 27-01-01"
	})
	void searchAssetsWithDateInTheFormatOfTheLanguageOfTheRequest(final String language, final String date) {
		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(SEARCH_PATH).queryParam("filter", "{filter}").build("validTo < '%s'".formatted(date)))
			.header(ACCEPT_LANGUAGE, language)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("searchAssets.filter", "'%s' is not a valid value for validTo.".formatted(date)));
		verifyNoInteractions(assetServiceMock);
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', quoteCharacter = '"', value = {
		"status                          | status is not a supported condition.",
		"exp(100000) > 1                 | exp('100000') > '1' is not a supported condition.",
		"(issued) > 5                    | (issued) > '5' is not a supported condition.",
		"issued > status                 | issued > status is not a supported condition.",
		"issued ~ '2025*'                | The operator ~ cannot be used on issued.",
		"additionalParameters > 5        | The operator > cannot be used on additionalParameters.",
		"description ~ ['Park*']         | description ~ ['Park*'] is not supported."
	})
	void searchAssetsWithFilterThatIsNotASupportedCondition(final String filter, final String message) {
		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(SEARCH_PATH).queryParam("filter", "{filter}").build(filter))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getViolations()).singleElement()
			.satisfies(violation -> {
				assertThat(violation.field()).isEqualTo("searchAssets.filter");
				assertThat(violation.message()).startsWith(message);
			});
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void searchAssetsWithInvalidPaging() {
		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(SEARCH_PATH).queryParam("page", 0).queryParam("limit", 1001).build())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(
				tuple("page", "must be greater than or equal to 1"),
				tuple("limit", "Page limit cannot be greater than 1000"));
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void searchAssetsWithPageAfterTheLastThatCanBeFetched() {
		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(SEARCH_PATH).queryParam("page", 2147485).queryParam("limit", 1000).build())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("page", "must be at most 2147484 when limit is 1000"));
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void searchAssetsWithLastPageThatCanBeFetched() {
		// Arrange
		when(assetServiceMock.searchAssets(eq(MUNICIPALITY_ID), any(), any())).thenReturn(AssetPagedResponse.create());

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(SEARCH_PATH).queryParam("page", 2147484).queryParam("limit", 1000).build())
			.exchange()
			.expectStatus().isOk();

		// Assert
		verify(assetServiceMock).searchAssets(eq(MUNICIPALITY_ID), any(), eq(PageRequest.of(2147483, 1000, Sort.by(asc("created"), asc("id")))));
		verifyNoMoreInteractions(assetServiceMock);
	}

	@Test
	void searchAssetsInvalidMunicipalityId() {
		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/" + INVALID + "/assets/search").build())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("searchAssets.municipalityId", "not a valid municipality ID"));
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void getAsset() {
		// Arrange
		final var id = randomUUID().toString();
		final var asset = TestFactory.getAsset();

		when(assetServiceMock.getAsset(MUNICIPALITY_ID, id)).thenReturn(new VersionedAsset(asset, 3L));

		// Act
		final var result = webTestClient.get()
			.uri(PATH + "/{id}", id)
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentType(APPLICATION_JSON)
			.expectHeader()
			.valueEquals("ETag", "\"3\"")
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
		assertThat(response.getStatus().value()).isEqualTo(400);
		assertThat(response.getViolations().getFirst().message()).isEqualTo("not a valid UUID");
		assertThat(response.getViolations().getFirst().field()).isEqualTo("getAsset.id");
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
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getAsset.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createAsset() {

		// Arrange
		final var uuid = randomUUID().toString();
		final var assetRequest = TestFactory.getAssetCreateRequest(randomUUID().toString()).withStatusReason(null);

		when(assetServiceMock.createAsset(MUNICIPALITY_ID, assetRequest, null)).thenReturn(uuid);
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
		verify(assetServiceMock).createAsset(MUNICIPALITY_ID, assetRequest, null);
		verifyNoMoreInteractions(assetServiceMock);
	}

	@Test
	void createAssetTooLongTitle() {
		// Arrange
		final var assetRequest = TestFactory.getAssetCreateRequest(randomUUID().toString()).withStatusReason(null).withTitle("a".repeat(256));

		// Act
		final var response = webTestClient.post()
			.uri(PATH)
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("title", "size must be between 0 and 255"));
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createAssetWithSourceReference() {

		// Arrange
		final var uuid = randomUUID().toString();
		final var assetRequest = TestFactory.getAssetCreateRequest(randomUUID().toString()).withStatusReason(null);
		final var sourceReference = "LINK|1234;case;service;MY_NAMESPACE|";

		when(assetServiceMock.createAsset(MUNICIPALITY_ID, assetRequest, sourceReference)).thenReturn(uuid);
		doNothing().when(jsonSchemaValidationServiceMock).validate(anyString(), anyString(), any(JsonNode.class));

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH)
				.queryParam("sourceReference", sourceReference)
				.build())
			.bodyValue(assetRequest)
			.exchange()
			.expectStatus()
			.isCreated()
			.expectHeader().location("/" + MUNICIPALITY_ID + "/assets/" + uuid);

		// Assert
		verify(assetServiceMock).createAsset(MUNICIPALITY_ID, assetRequest, sourceReference);
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
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("jsonParameters[0]", "Bad Request: some-error"));
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createAssetWithDraftStatus() {
		// Arrange
		final var assetRequest = TestFactory.getAssetCreateRequest(randomUUID().toString()).withStatus(Status.DRAFT).withStatusReason(null);
		final var expectedJsonMessage = """
			{
				"detail": "DRAFT status is not allowed when creating a regular asset",
				"status" : 400,
				"title" : "Bad Request"
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
	void createAssetFaultyStatusReason() {

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
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("createAsset.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void updateAsset() {

		// Arrange
		final var id = randomUUID().toString();
		final var assetRequest = TestFactory.getAssetUpdateRequest().withStatus(Status.ACTIVE).withStatusReason("IRREGULARITY");

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
		verify(assetServiceMock).updateAsset(MUNICIPALITY_ID, id, assetRequest, null);
		verifyNoMoreInteractions(assetServiceMock);
	}

	@Test
	void updateAssetWithDraftStatus() {
		// Arrange
		final var id = randomUUID().toString();
		final var assetRequest = TestFactory.getAssetUpdateRequest().withStatus(Status.DRAFT);
		final var expectedJsonMessage = """
			{
				"detail": "Changing asset status to DRAFT is not allowed when updating a regular asset",
				"status" : 400,
				"title" : "Bad Request"
			}""";

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
	void updateAssetFaultyUUID() {

		// Arrange
		final var id = "imNotARealUUID";
		final var assetRequest = TestFactory.getAssetUpdateRequest().withStatus(Status.DRAFT);

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
		assertThat(response.getViolations().getFirst().field()).isEqualTo("updateAsset.id");
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getType()).isEqualTo(ConstraintViolationProblem.TYPE);

		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void updateAssetInvalidMunicipalityId() {

		// Arrange
		final var uuid = randomUUID().toString();
		final var assetRequest = TestFactory.getAssetUpdateRequest().withStatus(null);

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
	void copyAsset() {
		// Arrange
		final var id = randomUUID().toString();
		final var newId = randomUUID().toString();

		when(assetServiceMock.copyAsset(MUNICIPALITY_ID, id)).thenReturn(newId);

		// Act
		webTestClient.post()
			.uri(PATH + "/{id}", id)
			.exchange()
			.expectStatus()
			.isCreated()
			.expectHeader().location("/" + MUNICIPALITY_ID + "/assets/" + newId);

		// Assert
		verify(assetServiceMock).copyAsset(MUNICIPALITY_ID, id);
		verifyNoMoreInteractions(assetServiceMock);
	}

	@Test
	void copyAssetFaultyUUID() {
		// Arrange
		final var id = "imNotARealUUID";

		// Act
		final var response = webTestClient.post()
			.uri(PATH + "/{id}", id)
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus().value()).isEqualTo(400);
		assertThat(response.getViolations().getFirst().field()).isEqualTo("copyAsset.id");
		assertThat(response.getViolations().getFirst().message()).isEqualTo("not a valid UUID");
		verifyNoInteractions(assetServiceMock);
	}

	@Test
	void copyAssetInvalidMunicipalityId() {
		// Arrange
		final var uuid = randomUUID().toString();

		// Act
		final var response = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path("/" + INVALID + "/assets/" + uuid).build())
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
			.containsExactly(tuple("copyAsset.municipalityId", "not a valid municipality ID"));
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
		assertThat(response.getStatus().value()).isEqualTo(400);
		assertThat(response.getViolations().getFirst().message()).isEqualTo("not a valid UUID");
		assertThat(response.getViolations().getFirst().field()).isEqualTo("deleteAsset.id");
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
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("deleteAsset.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(assetServiceMock);
	}

	private static Stream<String> scalarAssetAttributes() {
		return Stream.of(Asset.class.getDeclaredFields())
			.filter(field -> !Modifier.isStatic(field.getModifiers()) && !field.isSynthetic())
			.filter(field -> !Collection.class.isAssignableFrom(field.getType()) && !Map.class.isAssignableFrom(field.getType()))
			.map(Field::getName);
	}

	private static Stream<Arguments> sortByParameters() {
		return Stream.of(
			arguments("sortBy[0]", "issued", Sort.by(asc("issued"), asc("id"))),
			arguments("sortBy", "", Sort.by(asc("created"), asc("id"))),
			arguments("sortDirection", "", Sort.by(asc("created"), asc("id"))),
			arguments("sortDirection", "DESC", Sort.by(desc("created"), desc("id"))));
	}
}
