package se.sundsvall.partyassets.integration.db.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.AssetEntity_;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class AssetSpecification {

	private AssetSpecification() {}

	public static Specification<AssetEntity> createAssetSpecification(final String municipalityId, final AssetSearchRequest request) {
		return ((root, query, criteriaBuilder) -> {

			final List<Predicate> predicates = new ArrayList<>();

			addEqualCriteria(AssetEntity_.MUNICIPALITY_ID, municipalityId, predicates, criteriaBuilder, root);
			addEqualCriteria(AssetEntity_.PARTY_ID, request.getPartyId(), predicates, criteriaBuilder, root);
			addEqualCriteria(AssetEntity_.ASSET_ID, request.getAssetId(), predicates, criteriaBuilder, root);
			addEqualCriteria(AssetEntity_.ORIGIN, request.getOrigin(), predicates, criteriaBuilder, root);
			addEqualCriteria(AssetEntity_.TYPE, request.getType(), predicates, criteriaBuilder, root);
			addEqualCriteria(AssetEntity_.ISSUED, request.getIssued(), predicates, criteriaBuilder, root);
			addEqualCriteria(AssetEntity_.VALID_TO, request.getValidTo(), predicates, criteriaBuilder, root);
			addEqualCriteria(AssetEntity_.STATUS, request.getStatus(), predicates, criteriaBuilder, root);
			addEqualCriteria(AssetEntity_.STATUS_REASON, request.getStatusReason(), predicates, criteriaBuilder, root);
			addEqualCriteria(AssetEntity_.DESCRIPTION, request.getDescription(), predicates, criteriaBuilder, root);

			if (isNotEmpty(request.getAdditionalParameters())) {
				final List<Predicate> parameterPredicates = createParameterPredicates(request, criteriaBuilder, root.joinMap(AssetEntity_.ADDITIONAL_PARAMETERS));
				predicates.add(criteriaBuilder.or(parameterPredicates.toArray(new Predicate[0])));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		});
	}

	private static List<Predicate> createParameterPredicates(final AssetSearchRequest request, final CriteriaBuilder criteriaBuilder, final MapJoin<AssetEntity, String, String> join) {
		return request.getAdditionalParameters().entrySet().stream()
			.map(entry -> criteriaBuilder.and(
				criteriaBuilder.equal(join.key(), entry.getKey()),
				criteriaBuilder.equal(join.value(), entry.getValue())))
			.toList();
	}

	private static void addEqualCriteria(final String attribute, final Object value, final List<Predicate> predicates, final CriteriaBuilder criteriaBuilder, final Root<AssetEntity> root) {
		Stream.of(value)
			.filter(String.class::isInstance)
			.findAny()
			.map(String.class::cast)
			.ifPresentOrElse(string -> {
				if (isNotBlank(string)) {
					predicates.add(criteriaBuilder.equal(root.get(attribute), string));
				}
			}, () -> {
				if (nonNull(value)) {
					predicates.add(criteriaBuilder.equal(root.get(attribute), value));
				}
			});
	}

}
