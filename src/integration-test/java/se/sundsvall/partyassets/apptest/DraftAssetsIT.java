package se.sundsvall.partyassets.apptest;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.api.model.Asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Draft assets integration tests.
 *
 * @see src/test/resources/db/scripts/assetsIT.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/draftAssetsIT/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/assetsIT.sql"
})
class DraftAssetsIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String REQUEST_FILE = "request.json";
	private static final String RESPONSE_FILE = "response.json";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/asset-drafts";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void test01_createDraftAssetPrivateParty() {
		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(ALL_VALUE))
			.withExpectedResponseHeader(LOCATION, List.of("^" + PATH + "(.*)$"))
			.sendRequest()
			.getResponseHeaders().getLocation();

		assertThat(location).isNotNull();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(location.getPath())
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_createDraftAssetWithOtherStatusThanDraft() {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getDraftAssetsForPrivateParty() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "?partyId=f2ef7992-7b01-4185-a7f8-cf97dc7f438f")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_updateAsset() {
		final var id = "abd6596f-45a0-4912-89e4-8cdcea9a043a";

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(PATH + "/" + id)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "/" + id)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_updateDraftAssetWithInvalidStatusReason() {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(PATH + "/" + "abd6596f-45a0-4912-89e4-8cdcea9a043a")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_updateDraftAssetNotFound() {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(PATH + "/" + "00000000-0000-0000-0000-000000000000")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_getDraftAssetsEmpty() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "?partyId=c5d21b57-c785-4d3c-8361-940cae999ff7")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test09_activateDraftToActive() {
		final var draftId = "a0000000-0000-0000-0000-000000000002";
		final var originalId = "a0000000-0000-0000-0000-000000000001";
		final var assetsPath = PATH.replace("asset-drafts", "assets");

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(PATH + "/" + draftId)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(assetsPath + "/" + draftId)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("active_response.json")
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(assetsPath + "/" + originalId)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("replaced_response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test11_activateDraftRecordsTheActivatingActor() {
		final var draftId = "a0000000-0000-0000-0000-000000000002";

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(PATH + "/" + draftId)
			.withHeader(Identifier.HEADER_NAME, "joe01doe; type=adAccount")
			.withContentType(APPLICATION_JSON)
			.withRequest("{\"status\":\"ACTIVE\"}")
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(jdbcTemplate.queryForObject("select actor from asset where id = ?", String.class, draftId)).isEqualTo("joe01doe");
		assertThat(jdbcTemplate.queryForObject("select count(*) from asset_revision where asset_id = ?", Long.class, draftId)).isZero();
	}

	@Test
	void test12_createAndUpdateDraftTitle() throws Exception {
		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.sendRequest()
			.getResponseHeaders().getLocation();

		assertThat(location).isNotNull();
		assertThat(titleOf(location.getPath())).isEqualTo("Tillfälligt tillstånd");

		patchTitle(location.getPath(), "Stadigvarande tillstånd för servering av alkohol");
		assertThat(titleOf(location.getPath())).isEqualTo("Stadigvarande tillstånd för servering av alkohol");

		patchTitle(location.getPath(), "");
		assertThat(titleOf(location.getPath())).isNull();
	}

	private void patchTitle(final String path, final String title) {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(path)
			.withContentType(APPLICATION_JSON)
			.withRequest("{\"title\":\"" + title + "\"}")
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequest();
	}

	private String titleOf(final String path) throws Exception {
		return setupCall()
			.withHttpMethod(GET)
			.withServicePath(path)
			.withExpectedResponseStatus(OK)
			.sendRequest()
			.andReturnBody(Asset.class)
			.getTitle();
	}

	@Test
	void test10_activateDraftWithPastValidTo() {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(PATH + "/" + "abd6596f-45a0-4912-89e4-8cdcea9a043a")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test09_updateDraftAssetSetIndefinitely() {
		final var id = "abd6596f-45a0-4912-89e4-8cdcea9a043a";

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(PATH + "/" + id)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "/" + id)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_createDraftAssetPrivatePartyAndSourceReference() {
		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH + "?sourceReference=LINK|1234;case;service;MY_NAMESPACE|")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(ALL_VALUE))
			.withExpectedResponseHeader(LOCATION, List.of("^" + PATH + "(.*)$"))
			.sendRequest()
			.getResponseHeaders().getLocation();

		assertThat(location).isNotNull();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(location.getPath())
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
