package se.sundsvall.partyassets.integration.db.specification;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static se.sundsvall.partyassets.integration.db.model.AssetEntity_.ADDITIONAL_PARAMETERS;
import static se.sundsvall.partyassets.integration.db.model.AssetEntity_.ASSET_ID;
import static se.sundsvall.partyassets.integration.db.model.AssetEntity_.DESCRIPTION;
import static se.sundsvall.partyassets.integration.db.model.AssetEntity_.ISSUED;
import static se.sundsvall.partyassets.integration.db.model.AssetEntity_.PARTY_ID;
import static se.sundsvall.partyassets.integration.db.model.AssetEntity_.STATUS;
import static se.sundsvall.partyassets.integration.db.model.AssetEntity_.STATUS_REASON;
import static se.sundsvall.partyassets.integration.db.model.AssetEntity_.TYPE;
import static se.sundsvall.partyassets.integration.db.model.AssetEntity_.VALID_TO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

public class AssetSpecification {
	private AssetSpecification() {}

	public static Specification<AssetEntity> createAssetSpecification(final AssetSearchRequest request) {
		return ((root, query, criteriaBuilder) -> {

			final List<Predicate> predicates = new ArrayList<>();

			addEqualCriteria(PARTY_ID, request.getPartyId(), predicates, criteriaBuilder, root);
			addEqualCriteria(ASSET_ID, request.getAssetId(), predicates, criteriaBuilder, root);
			addEqualCriteria(TYPE, request.getType(), predicates, criteriaBuilder, root);
			addEqualCriteria(ISSUED, request.getIssued(), predicates, criteriaBuilder, root);
			addEqualCriteria(VALID_TO, request.getValidTo(), predicates, criteriaBuilder, root);
			addEqualCriteria(STATUS, request.getStatus(), predicates, criteriaBuilder, root);
			addEqualCriteria(STATUS_REASON, request.getStatusReason(), predicates, criteriaBuilder, root);
			addEqualCriteria(DESCRIPTION, request.getDescription(), predicates, criteriaBuilder, root);

			if (isNotEmpty(request.getAdditionalParameters())) {
				List<Predicate> parameterPredicates = createParameterPredicates(request, criteriaBuilder, root.joinMap(ADDITIONAL_PARAMETERS));
				predicates.add(criteriaBuilder.or(parameterPredicates.toArray(new Predicate[0])));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		});
	}

	private static List<Predicate> createParameterPredicates(AssetSearchRequest request, CriteriaBuilder criteriaBuilder, MapJoin<AssetEntity, String, String> join) {
		return request.getAdditionalParameters().entrySet().stream()
			.map(entry -> criteriaBuilder.and(
				criteriaBuilder.equal(join.key(), entry.getKey()),
				criteriaBuilder.equal(join.value(), entry.getValue())))
			.toList();
	}

	private static void addEqualCriteria(String attribute, Object value, List<Predicate> predicates, CriteriaBuilder criteriaBuilder, Root<AssetEntity> root) {
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
