package se.sundsvall.partyassets.apptest;

import java.sql.Timestamp;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.integration.db.AssetAttachmentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

/**
 * Asset attachment integration tests.
 *
 * @see src/test/resources/db/scripts/assetAttachmentsIT.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/assetAttachmentsIT/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/assetAttachmentsIT.sql"
})
class AssetAttachmentsIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ACTIVE_ASSET_ID = "e84b72ee-1a34-44b5-b8f6-2e0e42e99010";
	private static final String EXPIRED_ASSET_ID = "5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884";
	private static final String NON_EXISTING_ASSET_ID = "0fa4d59c-ee1f-4a5f-a5e7-6d0e19a05a83";
	private static final String FILE = "lokalritning.pdf";
	private static final String RESPONSE_FILE = "response.json";

	@Autowired
	private AssetAttachmentRepository attachmentRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static String path(final String assetId) {
		return "/" + MUNICIPALITY_ID + "/assets/" + assetId + "/attachments";
	}

	@Test
	void test01_createAttachment() throws Exception {
		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(path(ACTIVE_ASSET_ID))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^" + path(ACTIVE_ASSET_ID) + "/(.*)$"))
			.sendRequest()
			.getResponseHeaders()
			.getLocation();

		assertThat(location).isNotNull();

		assertThat(jdbcTemplate.queryForObject("select updated from asset where id = ?", Timestamp.class, ACTIVE_ASSET_ID)).isNotNull();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(location.getPath())
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_PDF_VALUE))
			.withExpectedResponseHeader(CONTENT_DISPOSITION, List.of("^attachment; filename=\"" + FILE + "\".*$"))
			.withExpectedBinaryResponse(FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_readAttachments() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(path(EXPIRED_ASSET_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_updateAttachment() throws Exception {
		final var attachmentId = createAttachmentOnActiveAsset();

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(path(ACTIVE_ASSET_ID) + "/" + attachmentId)
			.withContentType(APPLICATION_JSON)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.sendRequest();

		assertThat(attachmentRepository.findById(attachmentId)).hasValueSatisfying(attachment -> {
			assertThat(attachment.getUpdated()).isNotNull();
			assertThat(attachment.getFileName()).isEqualTo("ny-ritning.pdf");
			assertThat(attachment.getCategory()).isEqualTo("PLANRITNING");
			assertThat(attachment.getDescription()).isEqualTo("Uppdaterad ritning");
			assertThat(attachment.getMimeType()).isEqualTo(APPLICATION_PDF_VALUE);
		});
	}

	@Test
	void test04_deleteAttachment() throws Exception {
		final var attachmentId = createAttachmentOnActiveAsset();

		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(path(ACTIVE_ASSET_ID) + "/" + attachmentId)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(attachmentRepository.findById(attachmentId)).hasValueSatisfying(attachment -> assertThat(attachment.isDeleted()).isTrue());
		assertThat(attachmentRepository.findAllForAsset(ACTIVE_ASSET_ID, MUNICIPALITY_ID))
			.noneSatisfy(attachment -> assertThat(attachment.getId()).isEqualTo(attachmentId));
	}

	@Test
	void test05_createAttachmentOnExpiredAsset() throws Exception {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(path(EXPIRED_ASSET_ID))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", FILE)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_createAttachmentWithDisallowedContentType() throws Exception {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(path(ACTIVE_ASSET_ID))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", "anteckningar.txt")
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_createAttachmentOnNonExistingAsset() throws Exception {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(path(NON_EXISTING_ASSET_ID))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", FILE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_copiedAssetGetsItsOwnCopyOfTheAttachments() throws Exception {
		createAttachmentOnActiveAsset();
		final var dataRowsBeforeCopy = countAttachmentDataRows();

		final var draftLocation = setupCall()
			.withHttpMethod(POST)
			.withServicePath("/" + MUNICIPALITY_ID + "/assets/" + ACTIVE_ASSET_ID)
			.withExpectedResponseStatus(CREATED)
			.sendRequest()
			.getResponseHeaders()
			.getLocation();

		assertThat(draftLocation).isNotNull();
		final var draftId = draftLocation.getPath().substring(draftLocation.getPath().lastIndexOf('/') + 1);

		final var originalAttachments = attachmentRepository.findAllForAsset(ACTIVE_ASSET_ID, MUNICIPALITY_ID);
		final var copiedAttachments = attachmentRepository.findAllForAsset(draftId, MUNICIPALITY_ID);

		assertThat(originalAttachments).hasSize(1);
		assertThat(copiedAttachments).hasSize(1);
		assertThat(copiedAttachments.getFirst().getId()).isNotEqualTo(originalAttachments.getFirst().getId());
		assertThat(copiedAttachments.getFirst().getFileName()).isEqualTo(originalAttachments.getFirst().getFileName());
		assertThat(countAttachmentDataRows()).isEqualTo(dataRowsBeforeCopy + 1);

		assertThat(attachmentBytes(copiedAttachments.getFirst().getId()))
			.isNotEmpty()
			.isEqualTo(attachmentBytes(originalAttachments.getFirst().getId()));
	}

	@Test
	void test09_deleteAssetWithAttachments() throws Exception {
		createAttachmentOnActiveAsset();

		assertThat(attachmentRepository.findAllForAsset(ACTIVE_ASSET_ID, MUNICIPALITY_ID)).hasSize(1);
		final var dataRowsBeforeDelete = countAttachmentDataRows();

		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath("/" + MUNICIPALITY_ID + "/assets/" + ACTIVE_ASSET_ID)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(attachmentRepository.findAllForAsset(ACTIVE_ASSET_ID, MUNICIPALITY_ID)).isEmpty();
		assertThat(countAttachmentDataRows()).isEqualTo(dataRowsBeforeDelete - 1);
	}

	@Test
	void test10_deletedAttachmentIsStillDownloadable() throws Exception {
		final var attachmentId = createAttachmentOnActiveAsset();
		final var bytesBeforeDelete = attachmentBytes(attachmentId);
		final var dataRowsBeforeDelete = countAttachmentDataRows();

		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(path(ACTIVE_ASSET_ID) + "/" + attachmentId)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(path(ACTIVE_ASSET_ID) + "/" + attachmentId)
			.withExpectedResponseStatus(OK)
			.withExpectedBinaryResponse(FILE)
			.sendRequestAndVerifyResponse();

		assertThat(attachmentBytes(attachmentId)).isEqualTo(bytesBeforeDelete);
		assertThat(countAttachmentDataRows()).isEqualTo(dataRowsBeforeDelete);
	}

	@Test
	void test11_deletingAnAlreadyDeletedAttachmentReturnsNotFound() throws Exception {
		final var attachmentId = createAttachmentOnActiveAsset();

		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(path(ACTIVE_ASSET_ID) + "/" + attachmentId)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(path(ACTIVE_ASSET_ID) + "/" + attachmentId)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequest();
	}

	@Test
	void test12_attachmentChangesRecordRevisions() throws Exception {
		final var revisionsBefore = countRevisions(ACTIVE_ASSET_ID);
		final var attachmentId = createAttachmentOnActiveAsset();

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(path(ACTIVE_ASSET_ID) + "/" + attachmentId)
			.withContentType(APPLICATION_JSON)
			.withRequest("{\"fileName\":\"omdopt.pdf\"}")
			.withExpectedResponseStatus(OK)
			.sendRequest();

		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(path(ACTIVE_ASSET_ID) + "/" + attachmentId)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(countRevisions(ACTIVE_ASSET_ID)).isEqualTo(revisionsBefore + 3);
	}

	private long countRevisions(final String assetId) {
		return jdbcTemplate.queryForObject("select count(*) from asset_revision where asset_id = ?", Long.class, assetId);
	}

	private byte[] attachmentBytes(final String attachmentId) {
		return jdbcTemplate.queryForObject("""
			select d.file from asset_attachment_data d
			join asset_attachment a on a.asset_attachment_data_id = d.id
			where a.id = ?
			""", byte[].class, attachmentId);
	}

	private long countAttachmentDataRows() {
		return jdbcTemplate.queryForObject("select count(*) from asset_attachment_data", Long.class);
	}

	private String createAttachmentOnActiveAsset() throws Exception {
		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(path(ACTIVE_ASSET_ID))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", FILE)
			.withExpectedResponseStatus(CREATED)
			.sendRequest()
			.getResponseHeaders()
			.getLocation();

		assertThat(location).isNotNull();
		return location.getPath().substring(location.getPath().lastIndexOf('/') + 1);
	}
}
