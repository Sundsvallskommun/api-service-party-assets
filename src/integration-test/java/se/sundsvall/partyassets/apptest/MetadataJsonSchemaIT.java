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

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.partyassets.Application;

/**
 * MetadataJsonSchemaIT integration tests.
 *
 * @see src/test/resources/db/scripts/metadataJsonSchemaIT.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/metadataJsonSchemaIT/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/metadataJsonSchemaIT.sql"
})
class MetadataJsonSchemaIT extends AbstractAppTest {

	private static final String REQUEST_FILE = "request.json";
	private static final String RESPONSE_FILE = "response.json";
	private static final String MUNICIPALITY_ID = "2281";

	@Test
	void test01_createSchema() {
		final var location = setupCall()
			.withServicePath("/%s/metadata/jsonschemas".formatted(MUNICIPALITY_ID))
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.sendRequest()
			.getResponseHeaders().get(LOCATION).getFirst();

		setupCall()
			.withServicePath(location)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getSchemas() {
		setupCall()
			.withServicePath("/%s/metadata/jsonschemas".formatted(MUNICIPALITY_ID))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getSchema() {
		setupCall()
			.withServicePath("/%s/metadata/jsonschemas/%s".formatted(MUNICIPALITY_ID, "2281_schema_with_references_1.0.0"))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_deleteSchema() {
		setupCall()
			.withServicePath("/%s/metadata/jsonschemas/%s".formatted(MUNICIPALITY_ID, "2281_schema_1.0.0"))
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath("/%s/metadata/jsonschemas/%s".formatted(MUNICIPALITY_ID, "2281_schema_1.0.0"))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_deleteSchemaWithReferences() {
		setupCall()
			.withServicePath("/%s/metadata/jsonschemas/%s".formatted(MUNICIPALITY_ID, "2281_schema_with_references_1.0.0"))
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(CONFLICT)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_getLatestSchemaByName() {
		setupCall()
			.withServicePath("/%s/metadata/jsonschemas/%s/latest".formatted(MUNICIPALITY_ID, "schema"))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
