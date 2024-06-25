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
import se.sundsvall.partyassets.integration.db.model.PartyType;

public class AssetMapper {

	private AssetMapper() {}

	public static Asset toAsset(final AssetEntity entity) {
		return Asset.create()
			.withAdditionalParameters(entity.getAdditionalParameters())
			.withAssetId(entity.getAssetId())
			.withDescription(entity.getDescription())
			.withCaseReferenceIds(entity.getCaseReferenceIds())
			.withId(entity.getId())
			.withIssued(entity.getIssued())
			.withOrigin(entity.getOrigin())
			.withPartyId(entity.getPartyId())
			.withStatus(entity.getStatus())
			.withStatusReason(entity.getStatusReason())
			.withType(entity.getType())
			.withValidTo(entity.getValidTo());
	}

	public static AssetEntity toEntity(final AssetCreateRequest request, final PartyType partyType, final String municipalityId) {
		return AssetEntity.create()
			.withAdditionalParameters(request.getAdditionalParameters())
			.withAssetId(request.getAssetId())
			.withCaseReferenceIds(retreiveUniqueItems(request.getCaseReferenceIds())) // Filter out distinct values to save
			.withDescription(request.getDescription())
			.withIssued(request.getIssued())
			.withOrigin(request.getOrigin())
			.withPartyId(request.getPartyId())
			.withPartyType(partyType)
			.withStatus(request.getStatus())
			.withStatusReason(request.getStatusReason())
			.withType(request.getType())
			.withValidTo(request.getValidTo())
			.withMunicipalityId(municipalityId);
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
