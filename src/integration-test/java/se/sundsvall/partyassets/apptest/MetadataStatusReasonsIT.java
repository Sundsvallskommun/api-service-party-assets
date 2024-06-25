package se.sundsvall.partyassets.apptest;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.CREATED;
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

	@Autowired
	private StatusRepository repository;

	@Test
	void test01_createReasonsForStatus() {
		final var status = Status.EXPIRED;
		final var statusReasons = List.of("EARLY_RETIREMENT", "REACHED_RETIREMENT_AGE", "DEATH_BY_SNU_SNU");

		// Verify no existing status with status reasons before create
		assertThat(repository.findById(status.name())).isEmpty();

		// Create asset
		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/" + MUNICIPALITY_ID + "/metadata/statusreasons/%s".formatted(status.name()))
			.withRequest("request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/metadata/statusreasons/%s".formatted(status.name())))
			.sendRequestAndVerifyResponse();

		// Assert that status and corresponding status reasons has been created
		final var result = repository.findById(Status.EXPIRED.name());
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
			.andVerifyThat(() -> !repository.existsById(status.name()));
	}

}
