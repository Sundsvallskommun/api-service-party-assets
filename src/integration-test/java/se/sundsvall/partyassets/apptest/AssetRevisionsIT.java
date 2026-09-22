package se.sundsvall.partyassets.apptest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.partyassets.Application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Asset revision integration tests.
 *
 * @see src/test/resources/db/scripts/assetRevisionsIT.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/assetRevisionsIT/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/assetRevisionsIT.sql"
})
class AssetRevisionsIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ASSET_WITH_HISTORY = "e84b72ee-1a34-44b5-b8f6-2e0e42e99010";
	private static final String UNCHANGED_ASSET = "cba6f0e5-e826-4690-8776-37c69d981a2a";
	private static final String RESPONSE_FILE = "response.json";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static String revisionsPath(final String assetId) {
		return "/" + MUNICIPALITY_ID + "/assets/" + assetId + "/revisions";
	}

	@Test
	void test01_getRevisions() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(revisionsPath(ASSET_WITH_HISTORY))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getRevisionsForAnAssetThatWasNeverChanged() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(revisionsPath(UNCHANGED_ASSET))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getHistoricalRevision() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(revisionsPath(ASSET_WITH_HISTORY) + "/1")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_getCurrentRevision() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(revisionsPath(ASSET_WITH_HISTORY) + "/2")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_getRevisionThatDoesNotExist() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(revisionsPath(ASSET_WITH_HISTORY) + "/99")
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	// A change made over HTTP has to show up as a new revision carrying the actor from the header, and the previous
	// state has to be preserved untouched underneath it.
	@Test
	void test06_updatingAnAssetAddsARevision() {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath("/" + MUNICIPALITY_ID + "/assets/" + ASSET_WITH_HISTORY)
			.withHeader(Identifier.HEADER_NAME, "joe01doe; type=adAccount")
			.withContentType(APPLICATION_JSON)
			.withRequest("{\"status\":\"EXPIRED\"}")
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(revisionCount(ASSET_WITH_HISTORY)).isEqualTo(3);
		assertThat(currentActor(ASSET_WITH_HISTORY)).isEqualTo("joe01doe");
		// Revision 2 is the state from before this change, with the actor who created it.
		assertThat(actorOfRevision(ASSET_WITH_HISTORY, 2)).isEqualTo("third.actor");
		assertThat(statusOfRevision(ASSET_WITH_HISTORY, 2)).isEqualTo("BLOCKED");
	}

	// The story requires that deleting an asset leaves nothing readable behind. Only Flyway builds the cascading
	// foreign key, so this is the one place the rule is actually exercised.
	@Test
	void test07_deletingTheAssetRemovesItsRevisions() {
		assertThat(revisionCount(ASSET_WITH_HISTORY)).isPositive();

		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath("/" + MUNICIPALITY_ID + "/assets/" + ASSET_WITH_HISTORY)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(revisionCount(ASSET_WITH_HISTORY)).isZero();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(revisionsPath(ASSET_WITH_HISTORY))
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequest();
	}

	private long revisionCount(final String assetId) {
		return jdbcTemplate.queryForObject("select count(*) from asset_revision where asset_id = ?", Long.class, assetId);
	}

	private String currentActor(final String assetId) {
		return jdbcTemplate.queryForObject("select actor from asset where id = ?", String.class, assetId);
	}

	private String actorOfRevision(final String assetId, final int revision) {
		return jdbcTemplate.queryForObject("select actor from asset_revision where asset_id = ? and revision = ?", String.class, assetId, revision);
	}

	private String statusOfRevision(final String assetId, final int revision) {
		return jdbcTemplate.queryForObject("select status from asset_revision where asset_id = ? and revision = ?", String.class, assetId, revision);
	}
}
