package se.sundsvall.partyassets.api.validation.impl;

import com.turkraft.springfilter.converter.FilterSpecification;
import com.turkraft.springfilter.definition.FilterInfixOperator;
import com.turkraft.springfilter.language.EqualOperator;
import com.turkraft.springfilter.language.GreaterThanOperator;
import com.turkraft.springfilter.language.GreaterThanOrEqualOperator;
import com.turkraft.springfilter.language.InOperator;
import com.turkraft.springfilter.language.LessThanOperator;
import com.turkraft.springfilter.language.LessThanOrEqualOperator;
import com.turkraft.springfilter.language.NotEqualOperator;
import com.turkraft.springfilter.language.NotInOperator;
import com.turkraft.springfilter.parser.node.CollectionLikeNode;
import com.turkraft.springfilter.parser.node.CollectionNode;
import com.turkraft.springfilter.parser.node.FieldNode;
import com.turkraft.springfilter.parser.node.FilterNode;
import com.turkraft.springfilter.parser.node.InfixOperationNode;
import com.turkraft.springfilter.parser.node.InputNode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
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
	 * The types of the filterable attributes that the asset entity holds as something other than text or a collection.
	 */
	static final Map<String, Class<?>> TYPED_ATTRIBUTES = Map.of(
		"issued", LocalDate.class,
		"validTo", LocalDate.class,
		"created", OffsetDateTime.class,
		"updated", OffsetDateTime.class,
		"status", Status.class);

	private static final List<Class<? extends FilterInfixOperator>> COMPARISON_OPERATORS = List.of(
		EqualOperator.class, NotEqualOperator.class, GreaterThanOperator.class, GreaterThanOrEqualOperator.class,
		LessThanOperator.class, LessThanOrEqualOperator.class, InOperator.class, NotInOperator.class);

	private static final String UNKNOWN_ATTRIBUTES_TEMPLATE = "The attributes %s are not filterable. Filterable attributes are %s.";
	private static final String INVALID_VALUE_TEMPLATE = "'%s' is not a valid value for %s.";

	private final ConversionService conversionService;

	public ValidAssetFilterConstraintValidator(@Qualifier("sfConversionService") final ConversionService conversionService) {
		this.conversionService = conversionService;
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
			invalidValuesOf(filterSpecification.getFilter()).distinct())
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

	// SpringFilter compares a value that cannot be converted to the type of the attribute as text, which for dates and
	// timestamps gives a wrong result instead of an error
	private Stream<String> invalidValuesOf(final FilterNode node) {
		final var invalidValues = node instanceof final InfixOperationNode operation && isComparison(operation.getOperator())
			? Stream.concat(invalidValuesComparedWith(operation.getLeft(), operation.getRight()), invalidValuesComparedWith(operation.getRight(), operation.getLeft()))
			: Stream.<String>empty();
		return Stream.concat(invalidValues, childrenOf(node).flatMap(this::invalidValuesOf));
	}

	private Stream<String> invalidValuesComparedWith(final FilterNode attribute, final FilterNode values) {
		if (!(attribute instanceof final FieldNode fieldNode) || !TYPED_ATTRIBUTES.containsKey(fieldNode.getName())) {
			return Stream.empty();
		}
		final var type = TYPED_ATTRIBUTES.get(fieldNode.getName());
		return valuesOf(values)
			.filter(value -> !isConvertible(value, type))
			.map(value -> INVALID_VALUE_TEMPLATE.formatted(value, fieldNode.getName()));
	}

	private boolean isConvertible(final Object value, final Class<?> type) {
		try {
			conversionService.convert(value, type);
			return true;
		} catch (final ConversionException _) {
			return false;
		}
	}

	private static boolean isComparison(final FilterInfixOperator operator) {
		return COMPARISON_OPERATORS.stream().anyMatch(comparisonOperator -> comparisonOperator.isInstance(operator));
	}

	private static Stream<Object> valuesOf(final FilterNode node) {
		if (node instanceof final InputNode inputNode) {
			return Stream.of(inputNode.getValue());
		}
		if (node instanceof final CollectionNode collectionNode) {
			return collectionNode.getItems().stream().flatMap(ValidAssetFilterConstraintValidator::valuesOf);
		}
		return Stream.empty();
	}

	private static Stream<FilterNode> childrenOf(final FilterNode node) {
		// The children of a CollectionLikeNode leave out its patterns, which may refer to attributes as well
		return node instanceof final CollectionLikeNode collectionLikeNode
			? Stream.concat(collectionLikeNode.getChildren().stream(), collectionLikeNode.getPatterns().stream())
			: node.getChildren().stream();
	}
}
