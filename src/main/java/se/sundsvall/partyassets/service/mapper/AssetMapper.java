package se.sundsvall.partyassets.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;

import se.sundsvall.partyassets.api.model.Asset;
import se.sundsvall.partyassets.api.model.AssetCreateRequest;
import se.sundsvall.partyassets.api.model.AssetUpdateRequest;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

public class AssetMapper {

	private AssetMapper() {}

	public static Asset toAsset(final AssetEntity entity) {
		return Asset.create()
			.withId(entity.getId())
			.withAssetId(entity.getAssetId())
			.withPartyId(entity.getPartyId())
			.withCaseReferenceIds(entity.getCaseReferenceIds())
			.withType(entity.getType())
			.withIssued(entity.getIssued())
			.withValidTo(entity.getValidTo())
			.withStatus(entity.getStatus())
			.withStatusReason(entity.getStatusReason())
			.withDescription(entity.getDescription())
			.withAdditionalParameters(entity.getAdditionalParameters());
	}

	public static AssetEntity toEntity(final AssetCreateRequest request) {
		return AssetEntity.create()
			.withPartyId(request.getPartyId())
			.withAssetId(request.getAssetId())
			.withCaseReferenceIds(retreiveUniqueItems(request.getCaseReferenceIds())) // Filter out distinct values to save
			.withType(request.getType())
			.withIssued(request.getIssued())
			.withValidTo(request.getValidTo())
			.withStatus(request.getStatus())
			.withStatusReason(request.getStatusReason())
			.withDescription(request.getDescription())
			.withAdditionalParameters(request.getAdditionalParameters());
	}

	public static AssetEntity updateEntity(final AssetEntity entity, final AssetUpdateRequest request) {

		if (nonNull(request.getAdditionalParameters())) {
			entity.setAdditionalParameters(request.getAdditionalParameters());
		}

		if (nonNull(request.getCaseReferenceIds())) {
			entity.setCaseReferenceIds(retreiveUniqueItems(request.getCaseReferenceIds()));
		}

		if (nonNull(request.getStatus())) {
			entity.setStatus(request.getStatus());
		}

		if (nonNull(request.getStatusReason())) {
			entity.setStatusReason(request.getStatusReason());
		}

		if (nonNull(request.getValidTo())) {
			entity.setValidTo(request.getValidTo());
		}

		return entity;
	}

	private static ArrayList<String> retreiveUniqueItems(final List<String> list) {
		return new ArrayList<>(ofNullable(list).orElse(emptyList())
			.stream()
			.distinct()
			.toList());
	}
}
