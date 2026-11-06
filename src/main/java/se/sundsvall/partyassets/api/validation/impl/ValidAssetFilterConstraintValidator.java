package se.sundsvall.partyassets.api.validation.impl;

import com.turkraft.springfilter.converter.FilterSpecification;
import com.turkraft.springfilter.definition.FilterOperator;
import com.turkraft.springfilter.language.AndOperator;
import com.turkraft.springfilter.language.EqualOperator;
import com.turkraft.springfilter.language.ExistsFunction;
import com.turkraft.springfilter.language.GreaterThanOperator;
import com.turkraft.springfilter.language.GreaterThanOrEqualOperator;
import com.turkraft.springfilter.language.InOperator;
import com.turkraft.springfilter.language.InsensitiveLikeOperator;
import com.turkraft.springfilter.language.IsEmptyOperator;
import com.turkraft.springfilter.language.IsNotEmptyOperator;
import com.turkraft.springfilter.language.IsNotNullOperator;
import com.turkraft.springfilter.language.IsNullOperator;
import com.turkraft.springfilter.language.LessThanOperator;
import com.turkraft.springfilter.language.LessThanOrEqualOperator;
import com.turkraft.springfilter.language.LikeOperator;
import com.turkraft.springfilter.language.NotEqualOperator;
import com.turkraft.springfilter.language.NotInOperator;
import com.turkraft.springfilter.language.NotOperator;
import com.turkraft.springfilter.language.OrOperator;
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
import com.turkraft.springfilter.transformer.FilterStringTransformer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.api.validation.ValidAssetFilter;

import static java.util.Collections.unmodifiableSortedSet;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;
import static org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper.escapeMessageParameter;

/**
 * Accepts a filter that only refers to the attributes of the asset API model and only consists of conditions that
 * SpringFilter applies correctly to the asset entity: an attribute compared with values that it can hold, a text
 * attribute matched with one pattern, an attribute tested with {@code is null} or a collection with {@code is empty},
 * and {@code exists} of one condition, combined with {@code and}, {@code or}, {@code not} and parentheses.
 */
public class ValidAssetFilterConstraintValidator implements ConstraintValidator<ValidAssetFilter, Specification<?>> {

	/**
	 * The attribute paths of the asset API model. Each one must also be an attribute path of the asset entity, as the
	 * filter is applied to the entity.
	 */
	public static final SortedSet<String> FILTERABLE_ATTRIBUTES = unmodifiableSortedSet(new TreeSet<>(List.of(
		"id", "assetId", "origin", "partyId", "type", "issued", "validTo", "status", "statusReason", "description", "title", "replacesId", "created", "updated",
		"additionalParameters", "additionalParameters.key", "additionalParameters.value",
		"jsonParameters", "jsonParameters.key", "jsonParameters.value", "jsonParameters.schemaId")));

	/**
	 * The filterable attributes that the asset entity holds as a collection, which can only be tested for being empty.
	 */
	public static final Set<String> COLLECTION_ATTRIBUTES = Set.of("additionalParameters", "jsonParameters");

	/**
	 * The types of the filterable attributes that the asset entity holds as something other than text or a collection.
	 */
	static final Map<String, Class<?>> TYPED_ATTRIBUTES = Map.of(
		"issued", LocalDate.class,
		"validTo", LocalDate.class,
		"created", OffsetDateTime.class,
		"updated", OffsetDateTime.class,
		"status", Status.class);

	private static final List<Class<? extends FilterOperator>> LOGICAL_OPERATORS = List.of(AndOperator.class, OrOperator.class);
	private static final List<Class<? extends FilterOperator>> COMPARISON_OPERATORS = List.of(
		EqualOperator.class, NotEqualOperator.class, GreaterThanOperator.class, GreaterThanOrEqualOperator.class, LessThanOperator.class, LessThanOrEqualOperator.class);
	private static final List<Class<? extends FilterOperator>> LIST_OPERATORS = List.of(InOperator.class, NotInOperator.class);
	private static final List<Class<? extends FilterOperator>> LIKE_OPERATORS = List.of(LikeOperator.class, InsensitiveLikeOperator.class);
	private static final List<Class<? extends FilterOperator>> NULL_OPERATORS = List.of(IsNullOperator.class, IsNotNullOperator.class);
	private static final List<Class<? extends FilterOperator>> EMPTY_OPERATORS = List.of(IsEmptyOperator.class, IsNotEmptyOperator.class);

	private static final String UNKNOWN_ATTRIBUTES_TEMPLATE = "The attributes %s are not filterable. Filterable attributes are %s.";
	private static final String UNSUPPORTED_CONDITION_TEMPLATE = "%s is not a supported condition. Conditions test an attribute, such as validTo < '2027-01-01' or validTo is null, "
		+ "and are combined with and, or and not.";
	private static final String UNSUPPORTED_OPERATOR_TEMPLATE = "The operator %s cannot be used on %s.";
	private static final String LIKE_WITH_LIST_TEMPLATE = "%s is not supported. The operators ~ and ~~ take one pattern, so combine several with or.";
	private static final String INVALID_VALUE_TEMPLATE = "'%s' is not a valid value for %s.";

	private final ConversionService conversionService;
	private final FilterStringTransformer filterStringTransformer;

	public ValidAssetFilterConstraintValidator(@Qualifier("sfConversionService") final ConversionService conversionService) {
		this.conversionService = conversionService;
		this.filterStringTransformer = new FilterStringTransformer(conversionService);
	}

	@Override
	public boolean isValid(final Specification<?> filter, final ConstraintValidatorContext context) {
		if (!(filter instanceof final FilterSpecification<?> filterSpecification) || filterSpecification.getFilter() == null) {
			return true;
		}

		final var unknownAttributes = attributesOf(filterSpecification.getFilter())
			.filter(not(FILTERABLE_ATTRIBUTES::contains))
			.collect(toCollection(TreeSet::new));
		final var violations = Stream.concat(
			unknownAttributes.isEmpty() ? Stream.empty() : Stream.of(UNKNOWN_ATTRIBUTES_TEMPLATE.formatted(unknownAttributes, FILTERABLE_ATTRIBUTES)),
			violationsOfCondition(filterSpecification.getFilter()).distinct())
			.toList();

		if (violations.isEmpty()) {
			return true;
		}

		context.disableDefaultConstraintViolation();
		// The messages hold attribute names and values from the request, so they must not be interpreted as message expressions
		violations.forEach(violation -> context.buildConstraintViolationWithTemplate(escapeMessageParameter(violation)).addConstraintViolation());
		return false;
	}

	private static Stream<String> attributesOf(final FilterNode node) {
		if (node instanceof final FieldNode fieldNode) {
			return Stream.of(fieldNode.getName());
		}
		return childrenOf(node).flatMap(ValidAssetFilterConstraintValidator::attributesOf);
	}

	// SpringFilter turns any expression into a query, and one that is not such a condition, or that wraps an attribute or a
	// value in a function or parentheses, gives a wrong result or a database error instead of a 400
	private Stream<String> violationsOfCondition(final FilterNode node) {
		return switch (node) {
			case final PriorityNode priority -> violationsOfCondition(priority.getNode());
			case final PrefixOperationNode prefix when prefix.getOperator() instanceof NotOperator -> violationsOfCondition(prefix.getRight());
			case final InfixOperationNode infix when isAnyOf(infix.getOperator(), LOGICAL_OPERATORS) -> Stream.concat(violationsOfCondition(infix.getLeft()), violationsOfCondition(infix.getRight()));
			case final InfixOperationNode infix when isAnyOf(infix.getOperator(), COMPARISON_OPERATORS) -> violationsOfComparison(infix, infix.getRight() instanceof InputNode);
			case final InfixOperationNode infix when isAnyOf(infix.getOperator(), LIST_OPERATORS) -> violationsOfComparison(infix, isValueOrListOfValues(infix.getRight()));
			case final InfixOperationNode infix when isAnyOf(infix.getOperator(), LIKE_OPERATORS) -> violationsOfLike(infix);
			// SpringFilter matches a list of patterns without turning * into a wildcard, unlike a single pattern
			case final CollectionLikeNode collectionLike -> Stream.of(LIKE_WITH_LIST_TEMPLATE.formatted(text(collectionLike)));
			case final PostfixOperationNode postfix when isAnyOf(postfix.getOperator(), NULL_OPERATORS) -> violationsOfTest(postfix, not(COLLECTION_ATTRIBUTES::contains));
			case final PostfixOperationNode postfix when isAnyOf(postfix.getOperator(), EMPTY_OPERATORS) -> violationsOfTest(postfix, COLLECTION_ATTRIBUTES::contains);
			// SpringFilter applies only the first argument of exists
			case final FunctionNode function when function.getFunction() instanceof ExistsFunction && function.getArguments().size() == 1 -> violationsOfCondition(function.getArguments().getFirst());
			default -> Stream.of(UNSUPPORTED_CONDITION_TEMPLATE.formatted(text(node)));
		};
	}

	private Stream<String> violationsOfComparison(final InfixOperationNode comparison, final boolean comparesWithValues) {
		if (!(comparison.getLeft() instanceof final FieldNode attribute) || !comparesWithValues) {
			return Stream.of(UNSUPPORTED_CONDITION_TEMPLATE.formatted(text(comparison)));
		}
		if (COLLECTION_ATTRIBUTES.contains(attribute.getName())) {
			return Stream.of(UNSUPPORTED_OPERATOR_TEMPLATE.formatted(comparison.getOperator().getToken(), attribute.getName()));
		}
		return invalidValuesOf(attribute.getName(), comparison.getRight());
	}

	private Stream<String> violationsOfLike(final InfixOperationNode like) {
		if (!(like.getLeft() instanceof final FieldNode attribute) || !(like.getRight() instanceof InputNode)) {
			return Stream.of(UNSUPPORTED_CONDITION_TEMPLATE.formatted(text(like)));
		}
		if (COLLECTION_ATTRIBUTES.contains(attribute.getName()) || TYPED_ATTRIBUTES.containsKey(attribute.getName())) {
			return Stream.of(UNSUPPORTED_OPERATOR_TEMPLATE.formatted(like.getOperator().getToken(), attribute.getName()));
		}
		return Stream.empty();
	}

	private Stream<String> violationsOfTest(final PostfixOperationNode test, final Predicate<String> appliesTo) {
		if (!(test.getLeft() instanceof final FieldNode attribute)) {
			return Stream.of(UNSUPPORTED_CONDITION_TEMPLATE.formatted(text(test)));
		}
		// An attribute that is not filterable is reported once, among the attributes that are not filterable
		if (appliesTo.test(attribute.getName()) || !FILTERABLE_ATTRIBUTES.contains(attribute.getName())) {
			return Stream.empty();
		}
		return Stream.of(UNSUPPORTED_OPERATOR_TEMPLATE.formatted(test.getOperator().getToken(), attribute.getName()));
	}

	// SpringFilter compares a value that cannot be converted to the type of the attribute as text, which for dates and
	// timestamps gives a wrong result instead of an error
	private Stream<String> invalidValuesOf(final String attribute, final FilterNode values) {
		final var type = TYPED_ATTRIBUTES.get(attribute);
		if (type == null) {
			return Stream.empty();
		}
		return valuesOf(values)
			.filter(value -> !isValidValue(value, type))
			.map(value -> INVALID_VALUE_TEMPLATE.formatted(value, attribute));
	}

	private boolean isValidValue(final Object value, final Class<?> type) {
		try {
			// A blank value is converted to no value at all
			return conversionService.convert(value, type) != null;
		} catch (final ConversionException _) {
			return false;
		}
	}

	private String text(final FilterNode node) {
		return filterStringTransformer.transform(node);
	}

	private static boolean isAnyOf(final FilterOperator operator, final List<Class<? extends FilterOperator>> operators) {
		return operators.stream().anyMatch(type -> type.isInstance(operator));
	}

	private static boolean isValueOrListOfValues(final FilterNode node) {
		return node instanceof InputNode || node instanceof final CollectionNode collectionNode && collectionNode.getItems().stream().allMatch(InputNode.class::isInstance);
	}

	private static Stream<Object> valuesOf(final FilterNode node) {
		if (node instanceof final CollectionNode collectionNode) {
			return collectionNode.getItems().stream().flatMap(ValidAssetFilterConstraintValidator::valuesOf);
		}
		return Stream.of(((InputNode) node).getValue());
	}

	private static Stream<FilterNode> childrenOf(final FilterNode node) {
		// The children of a CollectionLikeNode leave out its patterns, which may refer to attributes as well
		return node instanceof final CollectionLikeNode collectionLikeNode
			? Stream.concat(collectionLikeNode.getChildren().stream(), collectionLikeNode.getPatterns().stream())
			: node.getChildren().stream();
	}
}
