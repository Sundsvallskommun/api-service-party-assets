package se.sundsvall.partyassets.integration.db;

import com.turkraft.springfilter.converter.FilterSpecification;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.stream.Stream;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.TestFactory;
import se.sundsvall.partyassets.api.model.AssetPagingParameters;
import se.sundsvall.partyassets.api.validation.impl.ValidAssetFilterConstraintValidator;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.specification.InvalidFilterException;

import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static se.sundsvall.partyassets.api.validation.impl.ValidAssetFilterConstraintValidator.COLLECTION_ATTRIBUTES;
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
	private static final Map<String, String> TYPED_VALUES = Map.of(
		"issued", "'2023-01-01'",
		"validTo", "'2023-12-31'",
		"created", "'2023-01-01T00:00:00+01:00'",
		"updated", "'2023-01-01T00:00:00Z'",
		"status", "'ACTIVE'");

	@Autowired
	private AssetRepository repository;

	@Autowired
	private FilterSpecificationConverter filterSpecificationConverter;

	@Autowired
	@Qualifier("sfConversionService")
	private ConversionService conversionService;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Test
	void everyKindOfConditionThatPassesValidationCanBeAppliedToEveryAttribute() {
		final var validator = new ValidAssetFilterConstraintValidator(conversionService);

		assertSoftly(softly -> conditionsOfEveryKind().forEach(condition -> {
			final FilterSpecification<AssetEntity> filter = filterSpecificationConverter.convert(condition);

			softly.assertThat(validator.isValid(filter, mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS))).as(condition).isTrue();
			softly.assertThatCode(() -> repository.findAll(withMunicipalityId(MUNICIPALITY_ID).and(withFilter(filter)), AssetPagingParameters.create().toPageable()))
				.as(condition)
				.doesNotThrowAnyException();
		}));
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', quoteCharacter = '"', value = {
		"status in ['ACTIVE', 'TEMPORARY']                  | 4",
		"status ! 'DRAFT'                                   | 7",
		"validTo < '2023-07-01' or validTo is null          | 3",
		"updated > '2023-03-01T00:00:00+01:00'              | 1",
		"jsonParameters is empty                            | 6",
		"jsonParameters is not empty                        | 2",
		"validTo between '2023-01-01' and '2023-12-31'      | 4",
		"description ~~ 'park*'                             | 4",
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
		"type : ['PERMIT', 'OTHER']", "issued ~ ['2023*']"
	})
	void filterThatCannotBeAppliedToTheEntity(final String filter) {
		assertThatExceptionOfType(InvalidFilterException.class)
			.isThrownBy(() -> search(filter, Pageable.unpaged()));
	}

	private Page<AssetEntity> search(final String filter, final Pageable pageable) {
		return repository.findAll(withMunicipalityId(MUNICIPALITY_ID).and(withFilter(filterSpecificationConverter.convert(filter))), pageable);
	}

	private static Stream<String> conditionsOfEveryKind() {
		return FILTERABLE_ATTRIBUTES.stream().flatMap(attribute -> {
			if (COLLECTION_ATTRIBUTES.contains(attribute)) {
				return Stream.of(attribute + " is empty", attribute + " is not empty");
			}
			final var value = TYPED_VALUES.getOrDefault(attribute, "'value'");
			final var comparisons = Stream.of(":", "!", ">", ">:", "<", "<:", "in", "not in")
				.map(operator -> operator.endsWith("in") ? "%s %s [%s]".formatted(attribute, operator, value) : "%s %s %s".formatted(attribute, operator, value));
			final var tests = Stream.of(
				attribute + " is null",
				attribute + " is not null",
				"%s between %s and %s".formatted(attribute, value, value),
				"exists(%s : %s)".formatted(attribute, value),
				"not exists(%s : %s)".formatted(attribute, value));
			final var patterns = TYPED_VALUES.containsKey(attribute) ? Stream.<String>empty() : Stream.of(attribute + " ~ '*val*'", attribute + " ~~ 'VAL*'");
			return Stream.of(comparisons, tests, patterns).flatMap(identity());
		});
	}
}
