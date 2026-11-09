package se.sundsvall.partyassets.api.model;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import se.sundsvall.dept44.models.api.paging.AbstractParameterPagingAndSortingBase;
import se.sundsvall.dept44.models.api.paging.validation.ValidSortByProperty;
import se.sundsvall.partyassets.api.validation.ValidPageOffset;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

import static java.util.Objects.requireNonNullElse;
import static org.springframework.data.domain.Sort.DEFAULT_DIRECTION;

/**
 * Paging and sorting parameters when searching assets. Sorts by {@code created} unless other properties are given, and
 * only allows sorting by the attributes of the asset model that hold a single value.
 */
@ValidSortByProperty(value = AssetEntity.class,
	include = {
		"id", "origin", "status", "statusReason", "description", "title", "created", "updated"
	},
	exclude = {
		"actor", "additionalParameters", "municipalityId", "revision", "version"
	})
@ValidPageOffset
public class AssetPagingParameters extends AbstractParameterPagingAndSortingBase {

	private static final String DEFAULT_SORT_PROPERTY = "created";
	private static final String TIEBREAKER_SORT_PROPERTY = "id";

	public AssetPagingParameters() {
		// Modifiable, as Spring binds an indexed parameter such as sortBy[0] by setting the element in this list
		sortBy = new ArrayList<>(List.of(DEFAULT_SORT_PROPERTY));
	}

	public static AssetPagingParameters create() {
		return new AssetPagingParameters();
	}

	public AssetPagingParameters withPage(final int page) {
		this.page = page;
		return this;
	}

	public AssetPagingParameters withLimit(final int limit) {
		this.limit = limit;
		return this;
	}

	public AssetPagingParameters withSortBy(final List<String> sortBy) {
		this.sortBy = sortBy;
		return this;
	}

	public AssetPagingParameters withSortDirection(final Sort.Direction sortDirection) {
		this.sortDirection = sortDirection;
		return this;
	}

	/**
	 * Converts the one-based page parameters into a {@link Pageable}, sorted by {@code created} if no sort properties are
	 * given. The sort always ends with {@code id} in the requested direction, so that assets with equal values for the
	 * requested sort properties keep the same order from one page to the next.
	 *
	 * @return                       the requested page
	 * @throws IllegalStateException if the page is less than 1
	 */
	public Pageable toPageable() {
		// The validation of the request already rejects such a page, but checking it here keeps page - 1 from underflowing
		if (page < 1) {
			throw new IllegalStateException("The page must be at least 1, but is " + page);
		}
		final var pageIndex = page - 1;

		final var direction = requireNonNullElse(sortDirection, DEFAULT_DIRECTION);
		final var requestedSort = sort();
		final var sort = requestedSort.isSorted() ? requestedSort : Sort.by(direction, DEFAULT_SORT_PROPERTY);
		final var stableSort = sort.getOrderFor(TIEBREAKER_SORT_PROPERTY) == null ? sort.and(Sort.by(direction, TIEBREAKER_SORT_PROPERTY)) : sort;
		return PageRequest.of(pageIndex, limit, stableSort);
	}

	@Override
	public String toString() {
		return "AssetPagingParameters [page=" + page + ", limit=" + limit + ", sortBy=" + sortBy + ", sortDirection=" + sortDirection + "]";
	}
}
