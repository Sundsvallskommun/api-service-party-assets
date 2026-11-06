package se.sundsvall.partyassets.api.model;

import jakarta.validation.Validation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import se.sundsvall.dept44.models.api.paging.validation.ValidSortByProperty;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.Order.desc;
import static se.sundsvall.partyassets.api.validation.impl.ValidAssetFilterConstraintValidator.COLLECTION_ATTRIBUTES;
import static se.sundsvall.partyassets.api.validation.impl.ValidAssetFilterConstraintValidator.FILTERABLE_ATTRIBUTES;

class AssetPagingParametersTest {

	@Test
	void testBean() {
		assertThat(AssetPagingParameters.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var bean = AssetPagingParameters.create()
			.withPage(3)
			.withLimit(25)
			.withSortBy(List.of("title"))
			.withSortDirection(DESC);

		assertThat(bean.getPage()).isEqualTo(3);
		assertThat(bean.getLimit()).isEqualTo(25);
		assertThat(bean.getSortBy()).containsExactly("title");
		assertThat(bean.getSortDirection()).isEqualTo(DESC);
	}

	@Test
	void defaults() {
		final var bean = AssetPagingParameters.create();

		assertThat(bean.getPage()).isEqualTo(1);
		assertThat(bean.getLimit()).isEqualTo(100);
		assertThat(bean.getSortBy()).containsExactly("created");
		assertThat(bean.getSortDirection()).isEqualTo(ASC);
	}

	@Test
	void toPageableWithDefaults() {
		assertThat(AssetPagingParameters.create().toPageable())
			.isEqualTo(PageRequest.of(0, 100, Sort.by(asc("created"), asc("id"))));
	}

	@Test
	void toPageableIsZeroBasedAndEndsWithId() {
		final var pageable = AssetPagingParameters.create()
			.withPage(3)
			.withLimit(25)
			.withSortBy(List.of("type", "validTo"))
			.withSortDirection(DESC)
			.toPageable();

		assertThat(pageable).isEqualTo(PageRequest.of(2, 25, Sort.by(desc("type"), desc("validTo"), desc("id"))));
	}

	@Test
	void toPageableKeepsRequestedSortOnId() {
		final var pageable = AssetPagingParameters.create()
			.withSortBy(List.of("id"))
			.withSortDirection(DESC)
			.toPageable();

		assertThat(pageable.getSort()).isEqualTo(Sort.by(desc("id")));
	}

	@ParameterizedTest
	@ValueSource(ints = {
		0, -1, Integer.MIN_VALUE
	})
	void toPageableWithPageLessThanOne(final int page) {
		final var bean = AssetPagingParameters.create().withPage(page);

		assertThatIllegalStateException()
			.isThrownBy(bean::toPageable)
			.withMessage("The page must be at least 1, but is " + page);
	}

	@Test
	void toPageableWithoutSortBySortsByCreated() {
		final var pageable = AssetPagingParameters.create()
			.withSortBy(List.of())
			.withSortDirection(DESC)
			.toPageable();

		assertThat(pageable.getSort()).isEqualTo(Sort.by(desc("created"), desc("id")));
	}

	@Test
	void toPageableWithoutSortDirectionSortsAscending() {
		final var pageable = AssetPagingParameters.create()
			.withSortBy(null)
			.withSortDirection(null)
			.toPageable();

		assertThat(pageable.getSort()).isEqualTo(Sort.by(asc("created"), asc("id")));
	}

	@Test
	void sortableAttributesAreTheFilterableAttributesThatHoldASingleValue() {
		final var validator = Validation.buildDefaultValidatorFactory().getValidator();
		final var candidates = new TreeSet<>(FILTERABLE_ATTRIBUTES);
		Stream.of(AssetEntity.class.getDeclaredFields()).map(Field::getName).forEach(candidates::add);

		final var sortable = candidates.stream()
			.filter(attribute -> validator.validate(AssetPagingParameters.create().withSortBy(List.of(attribute))).stream()
				.noneMatch(violation -> violation.getConstraintDescriptor().getAnnotation() instanceof ValidSortByProperty));

		assertThat(sortable).containsExactlyInAnyOrderElementsOf(FILTERABLE_ATTRIBUTES.stream()
			.filter(attribute -> !attribute.contains(".") && !COLLECTION_ATTRIBUTES.contains(attribute))
			.toList());
	}

	@Test
	void defaultSortByCanBeChangedByIndex() {
		final var bean = AssetPagingParameters.create();

		bean.getSortBy().set(0, "issued");

		assertThat(bean.toPageable().getSort()).isEqualTo(Sort.by(asc("issued"), asc("id")));
	}
}
