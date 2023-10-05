package se.sundsvall.partyassets.apptest;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.partyassets.integration.db.specification.AssetSpecification.createAssetSpecification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.model.PartyType;

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

	@Autowired
	private AssetRepository repository;

	@Test
	void test01_createAssetPrivateParty() {
		final var partyId = "a6c380f3-6d26-496d-93fe-10b1e0160354";
		final var searchRequestForPartyId = AssetSearchRequest.create().withPartyId(partyId);
		
		// Verify no existing assets on customer before create
		assertThat(repository.findOne(createAssetSpecification(searchRequestForPartyId))).isEmpty();
		
		// Create asset
		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/assets")
			.withRequest("request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^http://(.*)/assets/(.*)$"))
			.sendRequestAndVerifyResponse();
		
		// Verify that asset has been created for customer
		final var asset = repository.findOne(createAssetSpecification(searchRequestForPartyId));
		assertThat(asset).isPresent();
		assertThat(asset.get().getAdditionalParameters()).isNullOrEmpty();
		assertThat(asset.get().getAssetId()).isEqualTo("CON-0000000021");
		assertThat(asset.get().getCaseReferenceIds()).containsExactly("48649f4b-ec9f-4653-8586-14d69487c4be");
		assertThat(asset.get().getCreated()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(asset.get().getDescription()).isEqualTo("Bygglov");
		assertThat(asset.get().getId()).matches("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$");
		assertThat(asset.get().getIssued()).isEqualTo(LocalDate.of(2023, 1, 1));
		assertThat(asset.get().getPartyId()).isEqualTo(partyId);
		assertThat(asset.get().getPartyType()).isEqualTo(PartyType.PRIVATE);
		assertThat(asset.get().getStatus()).isEqualTo(Status.ACTIVE);
		assertThat(asset.get().getStatusReason()).isNull();
		assertThat(asset.get().getType()).isEqualTo("PERMIT");
		assertThat(asset.get().getUpdated()).isNull();
		assertThat(asset.get().getValidTo()).isEqualTo(LocalDate.of(2024, 12, 31));
	}

	@Test
	void test02_createAssetEnterpriseParty() {
		final var partyId = "b566819b-9133-4ba6-9ce2-f12aafbc047b";
		final var searchRequestForPartyId = AssetSearchRequest.create().withPartyId(partyId);

		// Verify no existing assets on customer before create
		assertThat(repository.findOne(createAssetSpecification(searchRequestForPartyId))).isEmpty();

		// Create asset
		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/assets")
			.withRequest("request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^http://(.*)/assets/(.*)$"))
			.sendRequestAndVerifyResponse();

		// Verify that asset has been created for customer
		final var asset = repository.findOne(createAssetSpecification(searchRequestForPartyId));
		assertThat(asset).isPresent();
		assertThat(asset.get().getAdditionalParameters()).isNullOrEmpty();
		assertThat(asset.get().getAssetId()).isEqualTo("CON-0000000055");
		assertThat(asset.get().getCaseReferenceIds()).containsExactly("391f7118-12d6-41c8-9032-4621c252000d");
		assertThat(asset.get().getCreated()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(asset.get().getDescription()).isEqualTo("Bygglov");
		assertThat(asset.get().getId()).matches("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$");
		assertThat(asset.get().getIssued()).isEqualTo(LocalDate.of(2023, 6, 1));
		assertThat(asset.get().getPartyId()).isEqualTo(partyId);
		assertThat(asset.get().getPartyType()).isEqualTo(PartyType.ENTERPRISE);
		assertThat(asset.get().getStatus()).isEqualTo(Status.ACTIVE);
		assertThat(asset.get().getStatusReason()).isNull();
		assertThat(asset.get().getType()).isEqualTo("PERMIT");
		assertThat(asset.get().getUpdated()).isNull();
		assertThat(asset.get().getValidTo()).isEqualTo(LocalDate.of(2024, 5, 31));
	}

	@Test
	void test03_createAssetNonExistingParty() {
		final var partyId = "0d2f16d7-c2b7-4b41-afa7-cefc819f0d6f";
		final var searchRequestForPartyId = AssetSearchRequest.create().withPartyId(partyId);

		// Create asset
		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/assets")
			.withRequest("request.json")
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();

		// Verify that no asset has been created for customer
		assertThat(repository.findOne(createAssetSpecification(searchRequestForPartyId))).isEmpty();
	}

	@Test
	void test04_findAllAssetsForPrivateParty() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath("/assets?partyId=f2ef7992-7b01-4185-a7f8-cf97dc7f438f")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_findSpecificAssetForPrivateParty() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath("/assets?partyId=f2ef7992-7b01-4185-a7f8-cf97dc7f438f" +
				"&additionalParameters[first_key]=third_value" +
				"&assetId=PRH-0000000002" +
				"&description=Parkeringstillstånd" +
				"&issued=2023-01-01" +
				"&status=BLOCKED" +
				"&statusReason=Stöldanmäld" +
				"&type=PERMIT" +
				"&validTo=2023-12-31")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_updateAsset() {
		final var id = "647e3062-62dc-499f-9faa-e54cb97aa214";

		// Verify asset before update
		final var assetPreUpdate = repository.findById(id).get();
		assertThat(assetPreUpdate.getAdditionalParameters()).isEmpty();
		assertThat(assetPreUpdate.getCaseReferenceIds()).isEmpty();
		assertThat(assetPreUpdate.getUpdated()).isNull();

		// Update asset
		setupCall()
			.withHttpMethod(PUT)
			.withServicePath("/assets/" + id)
			.withRequest("request.json")
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		// Verify asset after update
		final var assetPostUpdate = repository.findById(id).get();
		assertThat(assetPostUpdate).usingRecursiveComparison().ignoringFields("additionalParameters", "caseReferenceIds", "updated").isEqualTo(assetPreUpdate);
		assertThat(assetPostUpdate.getAdditionalParameters()).containsExactly(Map.entry("updated_key", "updated_value"));
		assertThat(assetPostUpdate.getCaseReferenceIds()).containsExactly("5965e286-5e11-450e-9da0-b749122f6280");
		assertThat(assetPostUpdate.getUpdated()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
	}

	@Test
	void test07_deleteAsset() {
		final var id = "7c145278-da81-49b0-a011-0f8f6821e3a0";

		// Verify existing asset before delete
		assertThat(repository.findById(id)).isPresent();

		// Update asset
		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath("/assets/" + id)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		// Verify non existing asset after delete
		assertThat(repository.findById(id)).isEmpty();
	}
}
