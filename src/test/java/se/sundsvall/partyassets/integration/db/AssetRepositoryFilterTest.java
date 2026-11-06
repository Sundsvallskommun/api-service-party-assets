package se.sundsvall.partyassets.integration.db;

import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import jakarta.persistence.EntityManagerFactory;
import java.util.stream.Stream;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.TestFactory;
import se.sundsvall.partyassets.api.model.AssetPagingParameters;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.specification.InvalidFilterException;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static se.sundsvall.partyassets.api.validation.impl.ValidAssetFilterConstraintValidator.FILTERABLE_ATTRIBUTES;
import static se.sundsvall.partyassets.integration.db.specification.AssetSpecification.withFilter;
import static se.sundsvall.partyassets.integration.db.specification.AssetSpecification.withMunicipalityId;

/**
 * Asset repository tests for searches with SpringFilter filters.
 *
 * @see src/test/resources/db/scripts/assetRepositoryTest.sql for data setup.
 */
// The whole application, since the filters are converted by the SpringFilter components
@SpringBootTest(classes = Application.class, webEnvironment = NONE)
@ActiveProfiles("junit")
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/assetRepositoryTest.sql"
})
class AssetRepositoryFilterTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Autowired
	private AssetRepository repository;

	@Autowired
	private FilterSpecificationConverter filterSpecificationConverter;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@ParameterizedTest
	@MethodSource("filterableAttributes")
	void everyFilterableAttributeIsAnAttributeOfTheEntity(final String attribute) {
		final var filter = attribute.equals("additionalParameters") || attribute.equals("jsonParameters") ? attribute + " is not empty" : attribute + " is not null";

		assertThat(search(filter, AssetPagingParameters.create().toPageable())).isNotNull();
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', quoteCharacter = '"', value = {
		"status in ['ACTIVE', 'TEMPORARY']                  | 4",
		"status ! 'DRAFT'                                   | 7",
		"validTo < '2023-07-01' or validTo is null          | 3",
		"updated > '2023-03-01T00:00:00+01:00'              | 1",
		"jsonParameters is empty                            | 6",
		"additionalParameters is not empty                  | 4",
		"description ~ '*tillstånd'                         | 4",
		"exists(additionalParameters.key : 'first_key' and additionalParameters.value : 'first_value') | 1",
		"not exists(additionalParameters.key : 'first_key') | 6"
	})
	void filtersFromTheReadme(final String filter, final long expectedTotal) {
		assertThat(search(filter, Pageable.unpaged()).getTotalElements()).isEqualTo(expectedTotal);
	}

	@Test
	void sortsByCreatedThenIdAndPages() {
		final var pageable = AssetPagingParameters.create().withLimit(3).toPageable();

		final var firstPage = search("type : 'PERMIT'", pageable);
		final var secondPage = search("type : 'PERMIT'", pageable.next());
		final var lastPage = search("type : 'PERMIT'", pageable.next().next());

		assertThat(firstPage.getTotalElements()).isEqualTo(8);
		assertThat(firstPage.getTotalPages()).isEqualTo(3);
		assertThat(firstPage.getContent()).extracting(AssetEntity::getAssetId).containsExactly("PRH-0000000001", "PRH-0000000011", "PRH-0000000002");
		assertThat(secondPage.getContent()).extracting(AssetEntity::getAssetId).containsExactly("TMP-0000000001", "CON-0000000003", "CON-0000000013");
		assertThat(lastPage.getContent()).extracting(AssetEntity::getAssetId).containsExactly("PRH-0000000012", "DFT-0000000001");
	}

	@Test
	void onlySearchesTheGivenMunicipality() {
		repository.save(TestFactory.getAssetEntity(null, randomUUID().toString()).withMunicipalityId("2262").withType("PERMIT"));

		assertThat(search("type : 'PERMIT'", Pageable.unpaged()).getTotalElements()).isEqualTo(8);
		assertThat(repository.findAll(withMunicipalityId("2262").and(filterSpecificationConverter.convert("type : 'PERMIT'")), Pageable.unpaged()).getTotalElements()).isOne();
	}

	@Test
	void includesDrafts() {
		assertThat(search("status : 'DRAFT'", Pageable.unpaged()).getContent())
			.extracting(AssetEntity::getAssetId)
			.containsExactly("DFT-0000000001");
	}

	@Test
	void conditionsInExistsMustHoldForTheSameParameter() {
		assertThat(search("exists(additionalParameters.key : 'first_key' and additionalParameters.value : 'third_value')", Pageable.unpaged()).getContent())
			.extracting(AssetEntity::getAssetId)
			.containsExactly("PRH-0000000002");
		// PRH-0000000001 has first_key with first_value, and second_value under another key
		assertThat(search("exists(additionalParameters.key : 'first_key' and additionalParameters.value : 'second_value')", Pageable.unpaged())).isEmpty();
		assertThat(search("jsonParameters is not empty and exists(additionalParameters.key : 'first_key' and additionalParameters.value : 'second_value')", Pageable.unpaged())).isEmpty();
	}

	@Test
	void notExistsFindsTheAssetsWithoutTheParameter() {
		assertThat(search("not exists(additionalParameters.key : 'first_key')", Pageable.unpaged()).getContent())
			.extracting(AssetEntity::getAssetId)
			.containsExactlyInAnyOrder("CON-0000000003", "PRH-0000000011", "PRH-0000000012", "CON-0000000013", "TMP-0000000001", "DFT-0000000001");
	}

	@Test
	void filterOnCollectionReturnsEachAssetOnce() {
		final var result = search("jsonParameters.key : 'first_key' or additionalParameters.value ~ '*value'", Pageable.unpaged());

		assertThat(result.getContent())
			.extracting(AssetEntity::getAssetId)
			.containsExactlyInAnyOrder("PRH-0000000001", "PRH-0000000002", "PRH-0000000011", "PRH-0000000012", "CON-0000000013");
		assertThat(result.getTotalElements()).isEqualTo(5);
	}

	@Test
	void loadsTheCollectionsOfAPageInBatches() {
		final var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
		statistics.clear();

		final var result = search("type : 'PERMIT'", AssetPagingParameters.create().toPageable());

		assertThat(result.getContent()).hasSize(8);
		// One query for the page, and one for each of the two collections, instead of two for every asset
		assertThat(statistics.getPrepareStatementCount()).isEqualTo(3);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"status : 'NOT_A_STATUS'", "type : ['PERMIT', 'OTHER']", "issued ~ ['2023*']"
	})
	void filterThatCannotBeAppliedToTheEntity(final String filter) {
		assertThatExceptionOfType(InvalidFilterException.class)
			.isThrownBy(() -> search(filter, Pageable.unpaged()));
	}

	private Page<AssetEntity> search(final String filter, final Pageable pageable) {
		return repository.findAll(withMunicipalityId(MUNICIPALITY_ID).and(withFilter(filterSpecificationConverter.convert(filter))), pageable);
	}

	private static Stream<String> filterableAttributes() {
		return FILTERABLE_ATTRIBUTES.stream();
	}
}
