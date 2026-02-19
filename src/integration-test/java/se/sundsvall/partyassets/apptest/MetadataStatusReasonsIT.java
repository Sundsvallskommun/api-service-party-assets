package se.sundsvall.partyassets.apptest;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.StatusRepository;
import se.sundsvall.partyassets.integration.db.model.StatusEntity;
import se.sundsvall.partyassets.integration.db.model.StatusEntityId;

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

	@Autowired
	private StatusRepository repository;

	@Test
	void test01_createReasonsForStatus() {
		final var status = Status.EXPIRED;
		final var statusReasons = List.of("EARLY_RETIREMENT", "REACHED_RETIREMENT_AGE", "DEATH_BY_SNU_SNU");

		// Verify no existing status with status reasons before create
		assertThat(repository.findById(new StatusEntityId(status.name(), MUNICIPALITY_ID))).isEmpty();

		// Create status reasons
		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons/%s".formatted(status.name()))
			.withRequest("request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/metadata/statusreasons/%s".formatted(status.name())))
			.sendRequestAndVerifyResponse();

		// Assert that status and corresponding status reasons has been created
		final var result = repository.findById(new StatusEntityId(Status.EXPIRED.name(), MUNICIPALITY_ID));
		assertThat(result).isPresent();
		assertThat(result.get().getCreated()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(result.get().getName()).isEqualTo(status.name());
		assertThat(result.get().getReasons()).hasSameSizeAs(statusReasons).hasSameElementsAs(statusReasons);
		assertThat(result.get().getUpdated()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS)); // Updated will be triggered when status reasons are added to the status entity
	}

	@Test
	void test02_readReasonsForStatus() {
		final var status = Status.BLOCKED;

		// Act and assert
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons/%s".formatted(status.name()))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_readReasonsForAllStatuses() {

		// Act and assert
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_deleteReasonsForStatus() {
		// Arrange
		final var status = Status.ACTIVE;

		repository.save(StatusEntity.create().withName(status.name()).withMunicipalityId(MUNICIPALITY_ID).withReasons(List.of("PLEASE_DONT_GET_DRUNK_THIS_CHRISTMAS", "YES_SIR_I_CAN_BOOGIE")));
		assertThat(repository.existsByNameAndMunicipalityId(status.name(), MUNICIPALITY_ID)).isTrue(); // Verify that status and status reasons has been saved

		// Act and assert
		setupCall()
			.withHttpMethod(HttpMethod.DELETE)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons/%s".formatted(status.name()))
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.andVerifyThat(() -> !repository.existsById(new StatusEntityId(status.name(), MUNICIPALITY_ID)));
	}

	@Test
	void test05_createReasonsForAlreadyExistingStatusReturnsConflict() {
		final var status = Status.BLOCKED; // BLOCKED already exists from SQL setup

		// Act and assert
		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons/%s".formatted(status.name()))
			.withRequest("request.json")
			.withExpectedResponseStatus(CONFLICT)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_deleteNonExistingReasonsReturnsNotFound() {
		final var status = Status.EXPIRED; // EXPIRED has no reasons in setup

		// Act and assert
		setupCall()
			.withHttpMethod(HttpMethod.DELETE)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons/%s".formatted(status.name()))
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_readNonExistingReasonsReturnsEmptyList() {
		final var status = Status.EXPIRED; // EXPIRED has no reasons in setup

		// Act and assert
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons/%s".formatted(status.name()))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_createReasonsInOtherMunicipalityDoesNotAffectOriginal() {
		final var status = Status.BLOCKED;
		final var otherReasons = List.of("OTHER_REASON_1", "OTHER_REASON_2");

		// Verify BLOCKED exists in municipality 2281 but not in 2262
		assertThat(repository.existsByNameAndMunicipalityId(status.name(), MUNICIPALITY_ID)).isTrue();
		assertThat(repository.existsByNameAndMunicipalityId(status.name(), OTHER_MUNICIPALITY_ID)).isFalse();

		// Create BLOCKED reasons in other municipality
		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/" + OTHER_MUNICIPALITY_ID + "/metadata/statusreasons/%s".formatted(status.name()))
			.withRequest("request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + OTHER_MUNICIPALITY_ID + "/metadata/statusreasons/%s".formatted(status.name())))
			.sendRequestAndVerifyResponse();

		// Verify both municipalities now have BLOCKED reasons with their own data
		final var original = repository.findById(new StatusEntityId(status.name(), MUNICIPALITY_ID));
		final var other = repository.findById(new StatusEntityId(status.name(), OTHER_MUNICIPALITY_ID));

		assertThat(original).isPresent();
		assertThat(original.get().getReasons()).containsExactlyInAnyOrder("IRREGULARITY", "LOST"); // unchanged

		assertThat(other).isPresent();
		assertThat(other.get().getReasons()).hasSameSizeAs(otherReasons).hasSameElementsAs(otherReasons);
	}

	@Test
	void test09_readReasonsFromOtherMunicipalityReturnsEmpty() {

		// Act and assert: reading BLOCKED reasons from municipality 2262 (which has no data) returns empty
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/" + OTHER_MUNICIPALITY_ID + "/metadata/statusreasons/%s".formatted(Status.BLOCKED.name()))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test10_deleteReasonsInOneMunicipalityDoesNotAffectOther() {
		final var status = Status.BLOCKED;

		// Arrange: create BLOCKED reasons in other municipality too
		repository.save(StatusEntity.create().withName(status.name()).withMunicipalityId(OTHER_MUNICIPALITY_ID).withReasons(List.of("TEMP_REASON")));
		assertThat(repository.existsByNameAndMunicipalityId(status.name(), MUNICIPALITY_ID)).isTrue();
		assertThat(repository.existsByNameAndMunicipalityId(status.name(), OTHER_MUNICIPALITY_ID)).isTrue();

		// Act: delete BLOCKED in the other municipality
		setupCall()
			.withHttpMethod(HttpMethod.DELETE)
			.withServicePath("/" + OTHER_MUNICIPALITY_ID + "/metadata/statusreasons/%s".formatted(status.name()))
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		// Assert: BLOCKED deleted in 2262 but still exists in 2281
		assertThat(repository.existsByNameAndMunicipalityId(status.name(), OTHER_MUNICIPALITY_ID)).isFalse();
		assertThat(repository.existsByNameAndMunicipalityId(status.name(), MUNICIPALITY_ID)).isTrue();
		assertThat(repository.findById(new StatusEntityId(status.name(), MUNICIPALITY_ID)).get().getReasons())
			.containsExactlyInAnyOrder("IRREGULARITY", "LOST"); // unchanged
	}

	@Test
	void test11_readAllReasonsFromOtherMunicipalityReturnsEmpty() {

		// Act and assert: reading all statuses from municipality 2262 returns empty map
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/" + OTHER_MUNICIPALITY_ID + "/metadata/statusreasons")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}
}
