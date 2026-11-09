package se.sundsvall.partyassets.api.validation.impl;

import com.turkraft.springfilter.converter.FilterSpecification;
import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.api.model.Asset;
import se.sundsvall.partyassets.api.model.AssetJsonParameter;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.partyassets.api.validation.impl.ValidAssetFilterConstraintValidator.COLLECTION_ATTRIBUTES;
import static se.sundsvall.partyassets.api.validation.impl.ValidAssetFilterConstraintValidator.FILTERABLE_ATTRIBUTES;
import static se.sundsvall.partyassets.api.validation.impl.ValidAssetFilterConstraintValidator.TYPED_ATTRIBUTES;

// The application, so that filters are parsed, and values converted, by the SpringFilter components it is configured with
@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class ValidAssetFilterConstraintValidatorTest {

	private static final String FILTERABLE_ATTRIBUTES_IN_MESSAGE = "[additionalParameters, additionalParameters.key, additionalParameters.value, assetId, created, description, id, issued, jsonParameters, "
		+ "jsonParameters.key, jsonParameters.schemaId, jsonParameters.value, origin, partyId, replacesId, status, statusReason, title, type, updated, validTo]";
	private static final String UNSUPPORTED_CONDITION = "%s is not a supported condition. Conditions test an attribute, such as validTo < '2027-01-01' or validTo is null, and are combined with and, or and not.";
	private static final String LIKE_WITH_LIST = "%s is not supported. The operators ~ and ~~ take one pattern, so combine several with or.";

	// A valid value of each typed attribute
	private static final Map<String, String> VALID_VALUES = Map.of(
		"issued", "2025-01-01",
		"validTo", "2025-12-31",
		"created", "2025-01-01T12:00:00+01:00",
		"updated", "2025-01-01T12:00:00Z",
		"status", "ACTIVE");

	@Autowired
	private FilterSpecificationConverter filterSpecificationConverter;

	@Autowired
	@Qualifier("sfConversionService")
	private ConversionService conversionService;

	@AfterEach
	void resetLocale() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	void missingFilterIsValid() {
		final var contextMock = mock(ConstraintValidatorContext.class);

		assertThat(validator().isValid(null, contextMock)).isTrue();

		verifyNoInteractions(contextMock);
	}

	@Test
	void emptyFilterIsValid() {
		final var contextMock = mock(ConstraintValidatorContext.class);
		final var filterMock = mock(FilterSpecification.class);
		when(filterMock.getFilter()).thenReturn(null);

		assertThat(validator().isValid(filterMock, contextMock)).isTrue();

		verifyNoInteractions(contextMock);
	}

	@Test
	void specificationThatIsNotAFilterIsValid() {
		final var contextMock = mock(ConstraintValidatorContext.class);
		final Specification<AssetEntity> specification = (_, _, criteriaBuilder) -> criteriaBuilder.conjunction();

		assertThat(validator().isValid(specification, contextMock)).isTrue();

		verifyNoInteractions(contextMock);
	}

	@ParameterizedTest
	@MethodSource("conditionsOnEveryAttribute")
	void conditionOnFilterableAttributeIsValid(final String filter) {
		assertThat(violationsOf(filter)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"not (status : 'DRAFT') and (jsonParameters is empty or additionalParameters is not empty)",
		"((type : 'A'))",
		"not not title is null",
		"exists(additionalParameters.key : 'errandId' and additionalParameters.value : '1234')",
		"not exists(additionalParameters.key : 'errandId') or exists(exists(jsonParameters.key : 'x'))",
		"status in [] or title not in []",
		"issued between '2025-01-01' and '2025-12-31'",
		"status = 'ACTIVE' and title like 'a*' and description ilike 'B*' and issued >= '2025-01-01' and validTo <> '2025-12-31'"
	})
	void combinedConditionsAreValid(final String filter) {
		assertThat(violationsOf(filter)).isEmpty();
	}

	@ParameterizedTest
	@EnumSource(Status.class)
	void everyStatusIsAValidValue(final Status status) {
		assertThat(violationsOf("status : '%s'".formatted(status))).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"status", "jsonParameters", "'abc'", "5", "true", "count(id)", "size(jsonParameters)", "exists()"
	})
	void filterThatIsNotAConditionIsInvalid(final String filter) {
		assertThat(violationsOf(filter)).containsExactly(UNSUPPORTED_CONDITION.formatted(textOf(filter)));
	}

	@Test
	void everyPartThatIsNotAConditionIsReported() {
		assertThat(violationsOf("5 and (6 or not title) or exists(status)")).containsExactly(
			UNSUPPORTED_CONDITION.formatted("'5'"),
			UNSUPPORTED_CONDITION.formatted("'6'"),
			UNSUPPORTED_CONDITION.formatted("title"),
			UNSUPPORTED_CONDITION.formatted("status"));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"(issued) > '2025-01-01'",
		"issued > ('2025-01-01')",
		"'2025-01-01' < issued",
		"issued > status",
		"issued < validTo",
		"localDate('abc') > issued",
		"exp(100000) > 1",
		"power(10, 1000000) > 1",
		"jsonText(title, 'a') : 'x'",
		"size(jsonParameters) : 1",
		"status : ['ACTIVE']",
		"status in [type]",
		"status in [('ACTIVE')]",
		"title ~ description",
		"title ~ []",
		"lower(title) ~ 'a*'",
		"(title) is null",
		"count(id) is not empty",
		"exists(title : 'a', title : 'b')",
		"status : 'ACTIVE' xor title : 'a'"
	})
	void conditionThatDoesNotTestAnAttributeWithValuesIsInvalid(final String filter) {
		assertThat(violationsOf(filter)).containsExactly(UNSUPPORTED_CONDITION.formatted(textOf(filter)));
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', quoteCharacter = '"', value = {
		"issued ~ '2025*'                  | ~            | issued",
		"status ~~ 'act*'                  | ~~           | status",
		"created like '2025*'              | ~            | created",
		"additionalParameters > 5          | >            | additionalParameters",
		"jsonParameters : 'x'              | :            | jsonParameters",
		"additionalParameters not in ['x'] | not in       | additionalParameters",
		"jsonParameters ~ 'x'              | ~            | jsonParameters",
		"additionalParameters is null      | is null      | additionalParameters",
		"jsonParameters is not null        | is not null  | jsonParameters",
		"title is empty                    | is empty     | title",
		"issued is not empty               | is not empty | issued",
		"additionalParameters.key is empty | is empty     | additionalParameters.key"
	})
	void operatorThatDoesNotApplyToTheAttributeIsInvalid(final String filter, final String operator, final String attribute) {
		assertThat(violationsOf(filter)).containsExactly("The operator %s cannot be used on %s.".formatted(operator, attribute));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"description ~ ['Park*']", "title ~~ ['a*', 'b*']"
	})
	void likeWithAListOfPatternsIsInvalid(final String filter) {
		assertThat(violationsOf(filter)).containsExactly(LIKE_WITH_LIST.formatted(textOf(filter)));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"actor", "revision", "version", "partyType", "municipalityId", "attachments", "attachments.attachmentData.file", "jsonParameters.asset.actor", "STATUS", "unknown"
	})
	void conditionOnAttributeOutsideTheAssetModelIsInvalid(final String attribute) {
		assertThat(violationsOf(attribute + " : 'value'"))
			.containsExactly("The attributes [%s] are not filterable. Filterable attributes are %s.".formatted(attribute, FILTERABLE_ATTRIBUTES_IN_MESSAGE));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"actor is null", "actor is empty", "actor ~ 'joe*'", "actor in ['joe01doe']", "exists(actor : 'joe01doe')"
	})
	void attributeOutsideTheAssetModelIsOnlyReportedAsNotFilterable(final String filter) {
		assertThat(violationsOf(filter))
			.containsExactly("The attributes [actor] are not filterable. Filterable attributes are %s.".formatted(FILTERABLE_ATTRIBUTES_IN_MESSAGE));
	}

	@Test
	void attributesOutsideTheAssetModelAreReportedOnceInOrderWhereverTheyAre() {
		assertThat(violationsOf("status : 'ACTIVE' and (version : 1 or not (actor : 'joe01doe' and version : 2)) or 'joe01doe' ~ [title, partyType]")).containsExactly(
			"The attributes [actor, partyType, version] are not filterable. Filterable attributes are %s.".formatted(FILTERABLE_ATTRIBUTES_IN_MESSAGE),
			LIKE_WITH_LIST.formatted("'joe01doe' ~ [title, partyType]"));
	}

	@ParameterizedTest
	@MethodSource("invalidValues")
	void valueThatTheAttributeCannotHoldIsInvalid(final String attribute, final String value) {
		assertThat(violationsOf("%s > '%s'".formatted(attribute, value))).containsExactly("'%s' is not a valid value for %s.".formatted(value, attribute));
	}

	@Test
	void everyValueOfAListIsCheckedAndEachInvalidValueReportedOnce() {
		assertThat(violationsOf("status in ['ACTIVE', 'FOO', 'FOO'] and validTo not in ['2025-13-45'] and issued between '2025-01-01' and ''")).containsExactly(
			"'FOO' is not a valid value for status.",
			"'2025-13-45' is not a valid value for validTo.",
			"'' is not a valid value for issued.");
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', quoteCharacter = '"', value = {
		"sv-SE | validTo | 27-01-01",
		"en-US | validTo | 1/1/27",
		"en-US | created | 1/1/27, 12:00 AM",
		"sv-SE | created | 2027-01-01 00:00"
	})
	void valueInAFormatOfTheLanguageOfTheRequestIsInvalid(final String language, final String attribute, final String value) {
		LocaleContextHolder.setLocale(Locale.forLanguageTag(language));

		assertThat(violationsOf("%s < '%s'".formatted(attribute, value))).containsExactly("'%s' is not a valid value for %s.".formatted(value, attribute));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"sv-SE", "en-US", "de-DE"
	})
	void isoValueIsValidWhateverTheLanguageOfTheRequest(final String language) {
		LocaleContextHolder.setLocale(Locale.forLanguageTag(language));

		assertThat(violationsOf("validTo < '2027-01-01' and created > '2027-01-01T00:00:00+01:00'")).isEmpty();
	}

	@Test
	void messagesAreNotInterpretedAsMessageExpressions() {
		final var contextMock = mock(ConstraintValidatorContext.class);
		final var violationBuilderMock = mock(ConstraintViolationBuilder.class);
		when(contextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilderMock);

		final var valid = validator().isValid(filterSpecificationConverter.convert("actor : 'x' and status : '${1+1}'"), contextMock);

		assertThat(valid).isFalse();
		verify(contextMock).disableDefaultConstraintViolation();
		verify(contextMock).buildConstraintViolationWithTemplate("The attributes [actor] are not filterable. Filterable attributes are %s.".formatted(FILTERABLE_ATTRIBUTES_IN_MESSAGE));
		verify(contextMock).buildConstraintViolationWithTemplate("'\\$\\{1+1\\}' is not a valid value for status.");
		verify(violationBuilderMock, times(2)).addConstraintViolation();
		verifyNoMoreInteractions(contextMock, violationBuilderMock);
	}

	@Test
	void filterableAttributesAreTheAttributesOfTheAssetModel() {
		final var expected = new TreeSet<String>();
		fieldsOf(Asset.class).map(Field::getName).forEach(expected::add);
		expected.addAll(List.of("additionalParameters.key", "additionalParameters.value"));
		fieldsOf(AssetJsonParameter.class).map(Field::getName).map("jsonParameters."::concat).forEach(expected::add);

		assertThat(FILTERABLE_ATTRIBUTES).containsExactlyElementsOf(expected);
	}

	@Test
	void collectionAttributesAreTheFilterableAttributesOfTheEntityThatAreCollections() {
		assertThat(fieldsOf(AssetEntity.class)
			.filter(field -> FILTERABLE_ATTRIBUTES.contains(field.getName()))
			.filter(field -> Collection.class.isAssignableFrom(field.getType()) || Map.class.isAssignableFrom(field.getType()))
			.map(Field::getName))
			.containsExactlyInAnyOrderElementsOf(COLLECTION_ATTRIBUTES);
	}

	@Test
	void typedAttributesAreTheFilterableAttributesOfTheEntityThatAreNeitherTextNorCollections() {
		final var expected = new HashMap<String, Class<?>>();
		fieldsOf(AssetEntity.class)
			.filter(field -> FILTERABLE_ATTRIBUTES.contains(field.getName()))
			.filter(field -> field.getType() != String.class && !COLLECTION_ATTRIBUTES.contains(field.getName()))
			.forEach(field -> expected.put(field.getName(), field.getType()));

		assertThat(TYPED_ATTRIBUTES).isEqualTo(expected);
		assertThat(TYPED_ATTRIBUTES.keySet()).containsExactlyInAnyOrderElementsOf(VALID_VALUES.keySet());
	}

	private ValidAssetFilterConstraintValidator validator() {
		return new ValidAssetFilterConstraintValidator(conversionService);
	}

	private List<String> violationsOf(final String filter) {
		final var contextMock = mock(ConstraintValidatorContext.class);
		final var violationBuilderMock = mock(ConstraintViolationBuilder.class);
		final var violations = new ArrayList<String>();
		when(contextMock.buildConstraintViolationWithTemplate(anyString())).thenAnswer(invocation -> {
			violations.add(invocation.getArgument(0));
			return violationBuilderMock;
		});

		final var valid = validator().isValid(filterSpecificationConverter.convert(filter), contextMock);

		assertThat(valid).isEqualTo(violations.isEmpty());
		return violations;
	}

	// The filter as SpringFilter writes it, which is how the messages quote it
	private String textOf(final String filter) {
		return filterSpecificationConverter.convert(filterSpecificationConverter.convert(filter));
	}

	private static Stream<String> conditionsOnEveryAttribute() {
		return FILTERABLE_ATTRIBUTES.stream().flatMap(attribute -> {
			if (COLLECTION_ATTRIBUTES.contains(attribute)) {
				return Stream.of(attribute + " is empty", attribute + " is not empty");
			}
			final var value = "'%s'".formatted(VALID_VALUES.getOrDefault(attribute, "value"));
			final var comparisons = Stream.of(":", "!", ">", ">:", "<", "<:")
				.map(operator -> "%s %s %s".formatted(attribute, operator, value));
			final var listsAndTests = Stream.of(
				"%s in [%s, %s]".formatted(attribute, value, value),
				"%s not in [%s]".formatted(attribute, value),
				"%s in %s".formatted(attribute, value),
				attribute + " is null",
				attribute + " is not null");
			final var patterns = TYPED_ATTRIBUTES.containsKey(attribute) ? Stream.<String>empty() : Stream.of(attribute + " ~ '*val*'", attribute + " ~~ 'VAL*'");
			return Stream.of(comparisons, listsAndTests, patterns).flatMap(identity());
		});
	}

	private static Stream<Arguments> invalidValues() {
		return Stream.of(
			arguments("issued", "2025-13-45"),
			arguments("issued", "5"),
			arguments("issued", ""),
			arguments("issued", " "),
			arguments("validTo", "tomorrow"),
			arguments("validTo", "2025-01-01T00:00:00"),
			arguments("created", "2025-01-01"),
			arguments("created", "2025-01-01T00:00:00"),
			arguments("created", ""),
			arguments("updated", "garbage"),
			arguments("status", "active"),
			arguments("status", "FOO"),
			arguments("status", ""));
	}

	private static Stream<Field> fieldsOf(final Class<?> type) {
		return Stream.of(type.getDeclaredFields())
			.filter(field -> !Modifier.isStatic(field.getModifiers()) && !field.isSynthetic());
	}
}
