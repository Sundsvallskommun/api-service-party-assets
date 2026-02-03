package se.sundsvall.partyassets.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.sundsvall.partyassets.api.model.Asset;
import se.sundsvall.partyassets.api.model.AssetCreateRequest;
import se.sundsvall.partyassets.api.model.AssetJsonParameter;
import se.sundsvall.partyassets.api.model.AssetUpdateRequest;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.AssetJsonParameterEntity;
import se.sundsvall.partyassets.integration.db.model.PartyType;

public final class AssetMapper {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private AssetMapper() {}

	public static Asset toAsset(final AssetEntity entity) {
		return Asset.create()
			.withAdditionalParameters(entity.getAdditionalParameters())
			.withAssetId(entity.getAssetId())
			.withDescription(entity.getDescription())
			.withCaseReferenceIds(entity.getCaseReferenceIds())
			.withId(entity.getId())
			.withIssued(entity.getIssued())
			.withJsonParameters(toAssetJsonParameterList(entity.getJsonParameters()))
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
			.withCaseReferenceIds(retrieveUniqueItems(request.getCaseReferenceIds())) // Filter out distinct values to save
			.withDescription(request.getDescription())
			.withIssued(request.getIssued())
			.addOrReplaceJsonParameters(toAssetJsonParameterEntityList(request.getJsonParameters()))
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
		Optional.ofNullable(request.getAdditionalParameters()).ifPresent(entity::setAdditionalParameters);
		Optional.ofNullable(request.getJsonParameters()).ifPresent(jsonParameters -> entity.addOrReplaceJsonParameters(toAssetJsonParameterEntityList(jsonParameters)));
		Optional.ofNullable(request.getCaseReferenceIds()).ifPresent(caseReferenceIds -> entity.setCaseReferenceIds(retrieveUniqueItems(caseReferenceIds)));
		Optional.ofNullable(request.getStatus()).ifPresent(entity::setStatus);
		Optional.ofNullable(request.getStatusReason()).ifPresent(entity::setStatusReason);
		Optional.ofNullable(request.getValidTo()).ifPresent(entity::setValidTo);

		return entity;
	}

	private static ArrayList<String> retrieveUniqueItems(final List<String> list) {
		return new ArrayList<>(ofNullable(list).orElse(emptyList())
			.stream()
			.distinct()
			.toList());
	}

	private static List<AssetJsonParameter> toAssetJsonParameterList(List<AssetJsonParameterEntity> assetJsonParameterEntityList) {
		return ofNullable(assetJsonParameterEntityList).orElse(emptyList()).stream()
			.map(AssetMapper::toAssetJsonParameter)
			.filter(Objects::nonNull)
			.toList();
	}

	private static AssetJsonParameter toAssetJsonParameter(AssetJsonParameterEntity assetJsonParameterEntity) {
		return Optional.ofNullable(assetJsonParameterEntity)
			.map(o -> AssetJsonParameter.create()
				.withKey(o.getKey())
				.withSchemaId(o.getSchemaId())
				.withValue(toJsonNode(o.getValue())))
			.orElse(null);
	}

	private static JsonNode toJsonNode(String value) {
		try {
			return value != null ? OBJECT_MAPPER.readTree(value) : null;
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to parse JSON value from database", e);
		}
	}

	private static List<AssetJsonParameterEntity> toAssetJsonParameterEntityList(List<AssetJsonParameter> assetJsonParameterList) {
		return ofNullable(assetJsonParameterList).orElse(emptyList()).stream()
			.map(AssetMapper::toAssetJsonParameterEntity)
			.filter(Objects::nonNull)
			.toList();
	}

	private static AssetJsonParameterEntity toAssetJsonParameterEntity(AssetJsonParameter assetJsonParameter) {
		return Optional.ofNullable(assetJsonParameter)
			.map(o -> AssetJsonParameterEntity.create()
				.withKey(o.getKey())
				.withSchemaId(assetJsonParameter.getSchemaId())
				.withValue(o.getValue() != null ? o.getValue().toString() : null))
			.orElse(null);
	}
}
