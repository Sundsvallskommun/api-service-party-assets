package se.sundsvall.partyassets.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.ALL_VALUE;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.partyassets.Application;

/**
 * Assets integration tests.
 *
 * @see src/test/resources/db/scripts/assetsIT.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/assetsIT/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/assetsIT.sql"
})
class AssetsIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String REQUEST_FILE = "request.json";
	private static final String RESPONSE_FILE = "response.json";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/assets";

	@Test
	void test01_createAssetPrivateParty() {
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
	void test02_createAssetEnterpriseParty() {
		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
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
	void test03_createAssetNonExistingParty() {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_findAllAssetsForPrivateParty() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "?partyId=f2ef7992-7b01-4185-a7f8-cf97dc7f438f")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_findSpecificAssetForPrivateParty() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath("""
				/2281/assets?partyId=f2ef7992-7b01-4185-a7f8-cf97dc7f438f\
				&additionalParameters[first_key]=third_value\
				&assetId=PRH-0000000002\
				&description=Parkeringstillstånd\
				&issued=2023-01-01\
				&status=BLOCKED\
				&statusReason=Stöldanmäld\
				&type=PERMIT\
				&validTo=2023-12-31""")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_updateAsset() {
		final var id = "647e3062-62dc-499f-9faa-e54cb97aa214";

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
	void test07_updateAssetWithInvalidStatusReason() {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(PATH + "/" + "647e3062-62dc-499f-9faa-e54cb97aa214")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_deleteAsset() {
		final var id = "7c145278-da81-49b0-a011-0f8f6821e3a0";

		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(PATH + "/" + id)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "/" + id)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test09_createAssetWithValidJsonParameters() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^" + PATH + "(.*)$"))
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test10_createAssetWithInvalidJsonParameters() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(BAD_REQUEST)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test11_updateAssetWithValidJsonParameters() {
		final var id = "e84b72ee-1a34-44b5-b8f6-2e0e42e99010";

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
	void test12_updateAssetWithInvalidJsonParameters() {
		final var id = "e84b72ee-1a34-44b5-b8f6-2e0e42e99010";

		// Update asset
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(PATH + "/" + id)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test13_createAssetWithJsonSchemaServerError() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test14_getAssetById() {
		final var id = "5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884";

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "/" + id)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test15_getAssetByIdNotFound() {
		final var id = "00000000-0000-0000-0000-000000000000";

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "/" + id)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test16_createAssetWithoutAssetId() {
		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
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
