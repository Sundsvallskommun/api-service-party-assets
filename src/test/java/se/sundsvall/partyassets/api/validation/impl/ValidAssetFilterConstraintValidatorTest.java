package se.sundsvall.partyassets.api.validation.impl;

import com.turkraft.springfilter.converter.FilterSpecification;
import com.turkraft.springfilter.definition.FilterInfixOperator;
import com.turkraft.springfilter.language.AndOperator;
import com.turkraft.springfilter.language.EqualOperator;
import com.turkraft.springfilter.language.GreaterThanOperator;
import com.turkraft.springfilter.language.GreaterThanOrEqualOperator;
import com.turkraft.springfilter.language.InOperator;
import com.turkraft.springfilter.language.InsensitiveLikeOperator;
import com.turkraft.springfilter.language.IsEmptyOperator;
import com.turkraft.springfilter.language.LessThanOperator;
import com.turkraft.springfilter.language.LessThanOrEqualOperator;
import com.turkraft.springfilter.language.LikeOperator;
import com.turkraft.springfilter.language.NotEqualOperator;
import com.turkraft.springfilter.language.NotInOperator;
import com.turkraft.springfilter.language.NotOperator;
import com.turkraft.springfilter.language.OrOperator;
import com.turkraft.springfilter.language.SizeFunction;
import com.turkraft.springfilter.parser.node.CollectionLikeNode;
import com.turkraft.springfilter.parser.node.CollectionNode;
import com.turkraft.springfilter.parser.node.FieldNode;
import com.turkraft.springfilter.parser.node.FilterNode;
import com.turkraft.springfilter.parser.node.FunctionNode;
import com.turkraft.springfilter.parser.node.InfixOperationNode;
import com.turkraft.springfilter.parser.node.InputNode;
import com.turkraft.springfilter.parser.node.PostfixOperationNode;
import com.turkraft.springfilter.parser.node.PrefixOperationNode;
import com.turkraft.springfilter.parser.node.PriorityNode;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.partyassets.api.model.Asset;
import se.sundsvall.partyassets.api.model.AssetJsonParameter;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.partyassets.api.validation.impl.ValidAssetFilterConstraintValidator.FILTERABLE_ATTRIBUTES;
import static se.sundsvall.partyassets.api.validation.impl.ValidAssetFilterConstraintValidator.TYPED_ATTRIBUTES;

@ExtendWith(MockitoExtension.class)
class ValidAssetFilterConstraintValidatorTest {

	private static final String FILTERABLE_ATTRIBUTES_IN_MESSAGE = "[additionalParameters, additionalParameters.key, additionalParameters.value, assetId, created, description, id, issued, jsonParameters, "
		+ "jsonParameters.key, jsonParameters.schemaId, jsonParameters.value, origin, partyId, replacesId, status, statusReason, title, type, updated, validTo]";

	// A valid value of each typed attribute, in the format SpringFilter converts
	private static final Map<String, String> VALID_VALUES = Map.of(
		"issued", "2025-01-01",
		"validTo", "2025-12-31",
		"created", "2025-01-01T12:00:00+01:00",
		"updated", "2025-01-01T12:00:00Z",
		"status", "ACTIVE");

	@Mock
	private ConstraintValidatorContext contextMock;

	@Mock
	private ConstraintViolationBuilder violationBuilderMock;

	@Mock
	private FilterSpecification<AssetEntity> filterMock;

	// The conversion service that SpringFilter uses in the application
	private final ValidAssetFilterConstraintValidator validator = new ValidAssetFilterConstraintValidator(new WebConversionService(new DateTimeFormatters()));

	@Test
	void missingFilterIsValid() {
		assertThat(validator.isValid(null, contextMock)).isTrue();

		verifyNoInteractions(contextMock);
	}

	@Test
	void emptyFilterIsValid() {
		when(filterMock.getFilter()).thenReturn(null);

		assertThat(validator.isValid(filterMock, contextMock)).isTrue();

		verifyNoInteractions(contextMock);
	}

	@Test
	void specificationThatIsNotAFilterIsValid() {
		final Specification<AssetEntity> specification = (_, _, criteriaBuilder) -> criteriaBuilder.conjunction();

		assertThat(validator.isValid(specification, contextMock)).isTrue();

		verifyNoInteractions(contextMock);
	}

	@ParameterizedTest
	@MethodSource("filterableAttributes")
	void filterOnFilterableAttributeIsValid(final String attribute) {
		when(filterMock.getFilter()).thenReturn(equal(attribute, VALID_VALUES.getOrDefault(attribute, "value")));

		assertThat(validator.isValid(filterMock, contextMock)).isTrue();

		verifyNoInteractions(contextMock);
	}

	@Test
	void filterWithEveryKindOfNodeOnFilterableAttributesIsValid() {
		// not (status : 'DRAFT') and (size(jsonParameters) : 1 or additionalParameters is empty) and type in ['A', 'B'] and
		// title ~ ['A*', description]
		when(filterMock.getFilter()).thenReturn(and(
			new PrefixOperationNode(new NotOperator(), new PriorityNode(equal("status", "DRAFT"))),
			and(
				new PriorityNode(new InfixOperationNode(
					new InfixOperationNode(new FunctionNode(new SizeFunction(), List.of(new FieldNode("jsonParameters"))), new EqualOperator(), new InputNode(1)),
					new OrOperator(),
					new PostfixOperationNode(new FieldNode("additionalParameters"), new IsEmptyOperator()))),
				and(
					new InfixOperationNode(new FieldNode("type"), new InOperator(), new CollectionNode(List.of(new InputNode("A"), new InputNode("B")))),
					new CollectionLikeNode(new FieldNode("title"), new LikeOperator(), List.of(new InputNode("A*"), new FieldNode("description")))))));

		assertThat(validator.isValid(filterMock, contextMock)).isTrue();

		verifyNoInteractions(contextMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"actor", "revision", "version", "partyType", "municipalityId", "attachments", "attachments.attachmentData.file", "jsonParameters.asset.actor", "STATUS", "unknown"
	})
	void filterOnAttributeOutsideTheAssetModelIsInvalid(final String attribute) {
		when(filterMock.getFilter()).thenReturn(equal(attribute, "value"));
		when(contextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilderMock);

		assertThat(validator.isValid(filterMock, contextMock)).isFalse();

		verify(contextMock).disableDefaultConstraintViolation();
		verify(contextMock).buildConstraintViolationWithTemplate("The attributes [%s] are not filterable. Filterable attributes are %s.".formatted(attribute, FILTERABLE_ATTRIBUTES_IN_MESSAGE));
		verify(violationBuilderMock).addConstraintViolation();
	}

	@Test
	void attributeAmongThePatternsOfALikeIsChecked() {
		// 'joe01doe' ~ [title, actor]
		when(filterMock.getFilter()).thenReturn(new CollectionLikeNode(new InputNode("joe01doe"), new LikeOperator(), List.of(new FieldNode("title"), new FieldNode("actor"))));
		when(contextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilderMock);

		assertThat(validator.isValid(filterMock, contextMock)).isFalse();

		verify(contextMock).buildConstraintViolationWithTemplate("The attributes [actor] are not filterable. Filterable attributes are %s.".formatted(FILTERABLE_ATTRIBUTES_IN_MESSAGE));
	}

	@Test
	void everyAttributeOutsideTheAssetModelIsReportedOnceInOrder() {
		// status : 'ACTIVE' and (version : 1 or not (actor : 'joe01doe' and version : 2))
		when(filterMock.getFilter()).thenReturn(and(
			equal("status", "ACTIVE"),
			new PriorityNode(new InfixOperationNode(
				equal("version", 1),
				new OrOperator(),
				new PrefixOperationNode(new NotOperator(), new PriorityNode(and(equal("actor", "joe01doe"), equal("version", 2))))))));
		when(contextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilderMock);

		assertThat(validator.isValid(filterMock, contextMock)).isFalse();

		verify(contextMock).buildConstraintViolationWithTemplate("The attributes [actor, version] are not filterable. Filterable attributes are %s.".formatted(FILTERABLE_ATTRIBUTES_IN_MESSAGE));
	}

	@Test
	void attributeNameIsNotInterpretedAsMessageExpression() {
		when(filterMock.getFilter()).thenReturn(equal("${1+1}", "value"));
		when(contextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilderMock);

		assertThat(validator.isValid(filterMock, contextMock)).isFalse();

		verify(contextMock).buildConstraintViolationWithTemplate("The attributes [\\$\\{1+1\\}] are not filterable. Filterable attributes are %s.".formatted(FILTERABLE_ATTRIBUTES_IN_MESSAGE));
	}

	@ParameterizedTest
	@MethodSource("comparisonOperators")
	void valueThatIsValidForTheTypeOfTheAttributeIsValid(final FilterInfixOperator operator) {
		when(filterMock.getFilter()).thenReturn(new InfixOperationNode(new FieldNode("issued"), operator, new InputNode("2025-01-01")));

		assertThat(validator.isValid(filterMock, contextMock)).isTrue();

		verifyNoInteractions(contextMock);
	}

	@ParameterizedTest
	@MethodSource("invalidValues")
	void valueThatIsNotValidForTheTypeOfTheAttributeIsInvalid(final String attribute, final FilterInfixOperator operator, final Object value) {
		when(filterMock.getFilter()).thenReturn(new InfixOperationNode(new FieldNode(attribute), operator, new InputNode(value)));
		when(contextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilderMock);

		assertThat(validator.isValid(filterMock, contextMock)).isFalse();

		verify(contextMock).disableDefaultConstraintViolation();
		verify(contextMock).buildConstraintViolationWithTemplate("'%s' is not a valid value for %s.".formatted(value, attribute));
		verify(violationBuilderMock).addConstraintViolation();
	}

	@Test
	void everyValueOfAListAndValueBeforeTheAttributeIsChecked() {
		// status in ['ACTIVE', 'FOO', 'FOO'] and '2025-13-45' < validTo
		when(filterMock.getFilter()).thenReturn(and(
			new InfixOperationNode(new FieldNode("status"), new InOperator(), new CollectionNode(List.of(new InputNode("ACTIVE"), new InputNode("FOO"), new InputNode("FOO")))),
			new InfixOperationNode(new InputNode("2025-13-45"), new LessThanOperator(), new FieldNode("validTo"))));
		when(contextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilderMock);

		assertThat(validator.isValid(filterMock, contextMock)).isFalse();

		verify(contextMock).buildConstraintViolationWithTemplate("'FOO' is not a valid value for status.");
		verify(contextMock).buildConstraintViolationWithTemplate("'2025-13-45' is not a valid value for validTo.");
		verify(contextMock).disableDefaultConstraintViolation();
		verifyNoMoreInteractions(contextMock);
	}

	@Test
	void patternOfALikeOnATypedAttributeIsNotChecked() {
		// issued ~ '2025*' or status ~~ ['act*']
		when(filterMock.getFilter()).thenReturn(new InfixOperationNode(
			new InfixOperationNode(new FieldNode("issued"), new LikeOperator(), new InputNode("2025*")),
			new OrOperator(),
			new CollectionLikeNode(new FieldNode("status"), new InsensitiveLikeOperator(), List.of(new InputNode("act*")))));

		assertThat(validator.isValid(filterMock, contextMock)).isTrue();

		verifyNoInteractions(contextMock);
	}

	@Test
	void typedAttributeComparedWithAnotherAttributeIsValid() {
		// issued < validTo
		when(filterMock.getFilter()).thenReturn(new InfixOperationNode(new FieldNode("issued"), new LessThanOperator(), new FieldNode("validTo")));

		assertThat(validator.isValid(filterMock, contextMock)).isTrue();

		verifyNoInteractions(contextMock);
	}

	@Test
	void unknownAttributesAndInvalidValuesAreBothReported() {
		// actor : 'joe01doe' and status : '${1+1}'
		when(filterMock.getFilter()).thenReturn(and(equal("actor", "joe01doe"), equal("status", "${1+1}")));
		when(contextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilderMock);

		assertThat(validator.isValid(filterMock, contextMock)).isFalse();

		verify(contextMock).buildConstraintViolationWithTemplate("The attributes [actor] are not filterable. Filterable attributes are %s.".formatted(FILTERABLE_ATTRIBUTES_IN_MESSAGE));
		verify(contextMock).buildConstraintViolationWithTemplate("'\\$\\{1+1\\}' is not a valid value for status.");
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
	void typedAttributesAreTheFilterableAttributesOfTheEntityThatAreNeitherTextNorCollections() {
		final var expected = new HashMap<String, Class<?>>();
		fieldsOf(AssetEntity.class)
			.filter(field -> FILTERABLE_ATTRIBUTES.contains(field.getName()))
			.filter(field -> field.getType() != String.class && !Collection.class.isAssignableFrom(field.getType()) && !Map.class.isAssignableFrom(field.getType()))
			.forEach(field -> expected.put(field.getName(), field.getType()));

		assertThat(TYPED_ATTRIBUTES).isEqualTo(expected);
		assertThat(VALID_VALUES).containsOnlyKeys(TYPED_ATTRIBUTES.keySet());
	}

	private static Stream<String> filterableAttributes() {
		return FILTERABLE_ATTRIBUTES.stream();
	}

	private static Stream<FilterInfixOperator> comparisonOperators() {
		return Stream.of(new EqualOperator(), new NotEqualOperator(), new GreaterThanOperator(), new GreaterThanOrEqualOperator(), new LessThanOperator(), new LessThanOrEqualOperator());
	}

	private static Stream<Arguments> invalidValues() {
		return Stream.of(
			arguments("issued", new GreaterThanOrEqualOperator(), "2025-13-45"),
			arguments("issued", new GreaterThanOperator(), 5),
			arguments("validTo", new LessThanOperator(), "tomorrow"),
			arguments("validTo", new LessThanOrEqualOperator(), "2025-01-01T00:00:00"),
			arguments("created", new GreaterThanOperator(), "2025-01-01"),
			arguments("updated", new NotEqualOperator(), "garbage"),
			arguments("status", new EqualOperator(), "active"),
			arguments("status", new NotInOperator(), "FOO"));
	}

	private static Stream<Field> fieldsOf(final Class<?> type) {
		return Stream.of(type.getDeclaredFields())
			.filter(field -> !Modifier.isStatic(field.getModifiers()) && !field.isSynthetic());
	}

	private static FilterNode equal(final String attribute, final Object value) {
		return new InfixOperationNode(new FieldNode(attribute), new EqualOperator(), new InputNode(value));
	}

	private static FilterNode and(final FilterNode left, final FilterNode right) {
		return new InfixOperationNode(left, new AndOperator(), right);
	}
}
