package se.sundsvall.citizenassets.integration.db.specification;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static se.sundsvall.citizenassets.integration.db.model.AssetEntity_.ASSET_ID;
import static se.sundsvall.citizenassets.integration.db.model.AssetEntity_.DESCRIPTION;
import static se.sundsvall.citizenassets.integration.db.model.AssetEntity_.ISSUED;
import static se.sundsvall.citizenassets.integration.db.model.AssetEntity_.PARTY_ID;
import static se.sundsvall.citizenassets.integration.db.model.AssetEntity_.STATUS;
import static se.sundsvall.citizenassets.integration.db.model.AssetEntity_.STATUS_REASON;
import static se.sundsvall.citizenassets.integration.db.model.AssetEntity_.TYPE;
import static se.sundsvall.citizenassets.integration.db.model.AssetEntity_.VALID_TO;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import se.sundsvall.citizenassets.api.model.AssetSearchRequest;
import se.sundsvall.citizenassets.integration.db.model.AssetEntity;

public class AssetSpecification {
	private AssetSpecification() {}

	public static Specification<AssetEntity> createAssetSpecification(AssetSearchRequest request) {
		return ((root, query, criteriaBuilder) -> {

			final List<Predicate> predicates = new ArrayList<>();

			predicates.add(criteriaBuilder.equal(root.get(PARTY_ID), request.getPartyId()));

			if (isNotBlank(request.getAssetId())) {
				predicates.add(criteriaBuilder.equal(root.get(ASSET_ID), request.getAssetId()));
			}

			if (isNotBlank(request.getType())) {
				predicates.add(criteriaBuilder.equal(root.get(TYPE), request.getType()));
			}

			if (nonNull(request.getIssued())) {
				predicates.add(criteriaBuilder.equal(root.get(ISSUED), request.getIssued()));
			}

			if (nonNull(request.getValidTo())) {
				predicates.add(criteriaBuilder.equal(root.get(VALID_TO), request.getValidTo()));
			}

			if (nonNull(request.getStatus())) {
				predicates.add(criteriaBuilder.equal(root.get(STATUS), request.getStatus()));
			}

			if (nonNull(request.getStatusReason())) {
				predicates.add(criteriaBuilder.equal(root.get(STATUS_REASON), request.getStatusReason()));
			}

			if (isNotBlank(request.getDescription())) {
				predicates.add(criteriaBuilder.equal(root.get(DESCRIPTION), request.getDescription()));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		});
	}
}
