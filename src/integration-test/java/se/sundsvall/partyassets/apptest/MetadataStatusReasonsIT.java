package se.sundsvall.partyassets.apptest;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.api.model.Status;

/**
 * MetadataStatusReasons integration tests.
 *
 * @see src/test/resources/db/scripts/metadataStatusReasonsIT.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/metadataStatusReasonsIT/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/metadataStatusReasonsIT.sql"
})
class MetadataStatusReasonsIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String OTHER_MUNICIPALITY_ID = "2262";
	private static final String REQUEST_FILE = "request.json";
	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test01_createReasonsForStatus() {
		final var status = Status.EXPIRED;
		final var path = "/" + MUNICIPALITY_ID + "/metadata/statusreasons/" + status.name();

		// Create status reasons
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(path)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^" + path))
			.sendRequest();

		// Verify created reasons via GET
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(path)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_readReasonsForStatus() {
		final var status = Status.BLOCKED;

		setupCall()
			.withHttpMethod(GET)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons/" + status.name())
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_readReasonsForAllStatuses() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_deleteReasonsForStatus() {
		final var status = Status.ACTIVE;
		final var path = "/" + MUNICIPALITY_ID + "/metadata/statusreasons/" + status.name();

		// Create status reasons via API
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(path)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.sendRequest();

		// Delete status reasons
		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(path)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		// Verify deletion via GET
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(path)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_createReasonsForAlreadyExistingStatusReturnsConflict() {
		final var status = Status.BLOCKED;

		setupCall()
			.withHttpMethod(POST)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons/" + status.name())
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CONFLICT)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_deleteNonExistingReasonsReturnsNotFound() {
		final var status = Status.EXPIRED;

		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons/" + status.name())
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_readNonExistingReasonsReturnsEmptyList() {
		final var status = Status.EXPIRED;

		setupCall()
			.withHttpMethod(GET)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons/" + status.name())
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_createReasonsInOtherMunicipalityDoesNotAffectOriginal() {
		final var status = Status.BLOCKED;

		// Create BLOCKED reasons in other municipality
		setupCall()
			.withHttpMethod(POST)
			.withServicePath("/" + OTHER_MUNICIPALITY_ID + "/metadata/statusreasons/" + status.name())
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + OTHER_MUNICIPALITY_ID + "/metadata/statusreasons/" + status.name()))
			.sendRequest();

		// Verify original municipality is unchanged
		setupCall()
			.withHttpMethod(GET)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons/" + status.name())
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();

		// Verify other municipality has new reasons
		setupCall()
			.withHttpMethod(GET)
			.withServicePath("/" + OTHER_MUNICIPALITY_ID + "/metadata/statusreasons/" + status.name())
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("other-response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test09_readReasonsFromOtherMunicipalityReturnsEmpty() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath("/" + OTHER_MUNICIPALITY_ID + "/metadata/statusreasons/" + Status.BLOCKED.name())
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test10_deleteReasonsInOneMunicipalityDoesNotAffectOther() {
		final var status = Status.BLOCKED;

		// Create BLOCKED reasons in other municipality
		setupCall()
			.withHttpMethod(POST)
			.withServicePath("/" + OTHER_MUNICIPALITY_ID + "/metadata/statusreasons/" + status.name())
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.sendRequest();

		// Delete BLOCKED in the other municipality
		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath("/" + OTHER_MUNICIPALITY_ID + "/metadata/statusreasons/" + status.name())
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		// Verify BLOCKED deleted in other municipality
		setupCall()
			.withHttpMethod(GET)
			.withServicePath("/" + OTHER_MUNICIPALITY_ID + "/metadata/statusreasons/" + status.name())
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("other-response.json")
			.sendRequest();

		// Verify BLOCKED still exists in original municipality
		setupCall()
			.withHttpMethod(GET)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons/" + status.name())
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test11_readAllReasonsFromOtherMunicipalityReturnsEmpty() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath("/" + OTHER_MUNICIPALITY_ID + "/metadata/statusreasons")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
