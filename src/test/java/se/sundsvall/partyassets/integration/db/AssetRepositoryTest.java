package se.sundsvall.partyassets.integration.db;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.partyassets.TestFactory;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.PartyType;
import se.sundsvall.partyassets.integration.db.specification.AssetSpecification;

/**
 * Asset repository tests.
 *
 * @see src/test/resources/db/scripts/AssetRepositoryTest.sql for data setup.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/assetRepositoryTest.sql"
})
class AssetRepositoryTest {

	private static final String PRIVATE_PARTY_1 = "f2ef7992-7b01-4185-a7f8-cf97dc7f438f";
	private static final String PRIVATE_PARTY_ASSET_ID_1 = "5d0aa6a4-e7ee-4dd4-9c3d-2aaeb689a884";
	private static final String PRIVATE_PARTY_ASSET_1 = "PRH-0000000001";
	private static final String PRIVATE_PARTY_ASSET_2 = "PRH-0000000002";
	private static final String PRIVATE_PARTY_ASSET_3 = "CON-0000000003";
	private static final String ENTERPRISE_PARTY_ASSET_ID_3 = "647e3062-62dc-499f-9faa-e54cb97aa214";
	private static final String MUNICIPALITY_ID = "2281";

	@Autowired
	private AssetRepository repository;

	@Test
	void testCreate() {
		final var uuid = UUID.randomUUID().toString();
		final var entity = TestFactory.getAssetEntity(null, uuid);

		repository.save(entity);

		assertThat(repository.findById(entity.getId())).isPresent();
	}

	@Test
	void testExistsByAssetIdAndMunicipalityId() {
		assertThat(repository.existsByAssetIdAndMunicipalityId(PRIVATE_PARTY_ASSET_1, MUNICIPALITY_ID)).isTrue();
		assertThat(repository.existsByAssetIdAndMunicipalityId("NON_EXISTING_ASSET_ID", MUNICIPALITY_ID)).isFalse();
	}

	@Test
	void testFindWithAllParameters() {
		final var request = AssetSearchRequest.create()
			.withAdditionalParameters(Map.of("first_key", "third_value"))
			.withAssetId(PRIVATE_PARTY_ASSET_2)
			.withOrigin("CASEDATA")
			.withDescription("Parkeringstillstånd")
			.withIssued(LocalDate.of(2023, 1, 1))
			.withPartyId(PRIVATE_PARTY_1)
			.withStatus(Status.BLOCKED)
			.withStatusReason("Stöldanmäld")
			.withType("PERMIT")
			.withValidTo(LocalDate.of(2023, 12, 31));

		final var result = repository.findAll(AssetSpecification.createAssetSpecification(MUNICIPALITY_ID, request));

		assertThat(result).hasSize(1)
			.extracting(
				AssetEntity::getAssetId,
				AssetEntity::getPartyId,
				AssetEntity::getPartyType)
			.containsExactly(tuple(
				PRIVATE_PARTY_ASSET_2,
				PRIVATE_PARTY_1,
				PartyType.PRIVATE));
	}

	@Test
	void testFindAllAssetsForCustomer() {
		final var request = AssetSearchRequest.create().withPartyId(PRIVATE_PARTY_1);
		final var result = repository.findAll(AssetSpecification.createAssetSpecification(MUNICIPALITY_ID, request));

		assertThat(result).hasSize(3)
			.extracting(AssetEntity::getAssetId)
			.containsExactlyInAnyOrder(PRIVATE_PARTY_ASSET_1, PRIVATE_PARTY_ASSET_2, PRIVATE_PARTY_ASSET_3);
	}

	@Test
	void findByIdAndMunicipalityId() {
		assertThat(repository.findByIdAndMunicipalityId(PRIVATE_PARTY_ASSET_ID_1, MUNICIPALITY_ID)).isPresent();
	}

	@Test
	void findByIdAndMunicipalityIdNotFound() {
		assertThat(repository.findByIdAndMunicipalityId("does-not-exist", MUNICIPALITY_ID)).isNotPresent();
	}

	@Test
	void countByJsonParametersSchemaId() {
		assertThat(repository.countByJsonParametersSchemaId("2281_person_schema_1.0.0")).isEqualTo(2);
	}

	@Test
	void countByJsonParametersSchemaIdWhenNothingIsFound() {
		assertThat(repository.countByJsonParametersSchemaId("non-existing-schema")).isZero();
	}

	@Test
	void testUpdate() {
		final var uuid = UUID.randomUUID().toString();
		final var entity = TestFactory.getAssetEntity(null, uuid)
			.withCreated(null)
			.withUpdated(null);

		final var persistedEntity = repository.saveAndFlush(entity);

		assertThat(persistedEntity).isEqualTo(entity);
		assertThat(persistedEntity.getCreated()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		assertThat(persistedEntity.getId()).isNotBlank();
		assertThat(persistedEntity.getUpdated()).isNull();

		persistedEntity.setDescription("Updated description");

		final var updatedEntity = repository.saveAndFlush(persistedEntity);

		assertThat(updatedEntity).isEqualTo(persistedEntity);
		assertThat(updatedEntity.getDescription()).isEqualTo("Updated description");
		assertThat(updatedEntity.getUpdated()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
	}

	@Test
	void testDelete() {
		assertThat(repository.findById(ENTERPRISE_PARTY_ASSET_ID_3)).isPresent();

		repository.deleteById(ENTERPRISE_PARTY_ASSET_ID_3);

		assertThat(repository.findById(ENTERPRISE_PARTY_ASSET_ID_3)).isNotPresent();
	}
}
