package se.sundsvall.citizenassets.integration.db.specification;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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

			predicates.add(criteriaBuilder.equal(root.get("partyId"), request.getPartyId()));

			if (isNotBlank(request.getAssetId())) {
				predicates.add(criteriaBuilder.equal(root.get("assetId"), request.getAssetId()));
			}

			if (isNotBlank(request.getType())) {
				predicates.add(criteriaBuilder.equal(root.get("type"), request.getType()));
			}

			if (nonNull(request.getIssued())) {
				predicates.add(criteriaBuilder.equal(root.get("issued"), request.getIssued()));
			}

			if (nonNull(request.getValidTo())) {
				predicates.add(criteriaBuilder.equal(root.get("validTo"), request.getValidTo()));
			}

			if (nonNull(request.getStatus())) {
				predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
			}

			if (nonNull(request.getStatusReason())) {
				predicates.add(criteriaBuilder.equal(root.get("statusReason"), request.getStatusReason()));
			}

			if (isNotBlank(request.getDescription())) {
				predicates.add(criteriaBuilder.equal(root.get("description"), request.getDescription()));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		});
	}
}
