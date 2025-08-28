package se.sundsvall.partyassets;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import se.sundsvall.partyassets.api.model.Asset;
import se.sundsvall.partyassets.api.model.AssetCreateRequest;
import se.sundsvall.partyassets.api.model.AssetJsonParameter;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.api.model.AssetUpdateRequest;
import se.sundsvall.partyassets.api.model.JsonSchemaCreateRequest;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.AssetJsonParameterEntity;
import se.sundsvall.partyassets.integration.db.model.JsonSchemaEntity;

public final class TestFactory {

	public static AssetEntity getAssetEntity(final String id, final String partyId) {
		final var assetEntity = AssetEntity.create()
			.withAdditionalParameters(new HashMap<>(Map.of("key", "value")))
			.withAssetId("assetId")
			.withMunicipalityId("municipalityId")
			.withCaseReferenceIds(new ArrayList<>(List.of("caseReferenceId")))
			.withCreated(OffsetDateTime.now().minusDays(7))
			.withDescription("description")
			.withId(id)
			.withIssued(LocalDate.of(2010, 1, 1))
			.withPartyId(partyId)
			.withStatus(Status.ACTIVE)
			.withStatusReason("statusReason")
			.withType("type")
			.withUpdated(OffsetDateTime.now())
			.withValidTo(LocalDate.of(2010, 1, 1));

		final var jsonParameters = new ArrayList<AssetJsonParameterEntity>();
		jsonParameters.add(AssetJsonParameterEntity.create()
			.withAsset(assetEntity)
			.withKey("key1")
			.withValue("{}")
			.withSchema(JsonSchemaEntity.create().withId("2281_person_schema_1.0.0")));

		assetEntity.withJsonParameters(jsonParameters);

		return assetEntity;
	}

	public static Asset getAsset() {
		return Asset.create()
			.withAdditionalParameters(Map.of("key", "value"))
			.withAssetId("assetId")
			.withCaseReferenceIds(List.of("caseReferenceId"))
			.withDescription("description")
			.withId(UUID.randomUUID().toString())
			.withIssued(LocalDate.of(2010, 1, 1))
			.withJsonParameters(List.of(AssetJsonParameter.create()
				.withKey("key1")
				.withSchemaId("2281_person_schema_1.0.0")
				.withValue("{}")))
			.withPartyId(UUID.randomUUID().toString())
			.withStatus(Status.ACTIVE)
			.withStatusReason("statusReason")
			.withType("type")
			.withValidTo(LocalDate.of(2010, 1, 1));
	}

	public static AssetCreateRequest getAssetCreateRequest(final String partyId) {
		return AssetCreateRequest.create()
			.withAdditionalParameters(Map.of("key", "value"))
			.withAssetId("assetId")
			.withCaseReferenceIds(List.of("caseReferenceId"))
			.withDescription("description")
			.withIssued(LocalDate.of(2010, 1, 1))
			.withJsonParameters(List.of(AssetJsonParameter.create()
				.withKey("key1")
				.withSchemaId("2281_person_schema_1.0.0")
				.withValue("{}")))
			.withPartyId(partyId)
			.withStatus(Status.ACTIVE)
			.withStatusReason("statusReason")
			.withType("type")
			.withValidTo(LocalDate.of(2010, 1, 1));
	}

	public static AssetUpdateRequest getAssetUpdateRequest() {
		return AssetUpdateRequest.create()
			.withAdditionalParameters(Map.of("key", "changed_value", "key2", "value2"))
			.withCaseReferenceIds(List.of("caseReferenceId", "caseReferenceId2"))
			.withJsonParameters(List.of(AssetJsonParameter.create()
				.withKey("key2")
				.withSchemaId("2281_person_schema_2.0.0")
				.withValue("{\"newAttribute\" : \"value\"}")))
			.withStatus(Status.BLOCKED)
			.withStatusReason("statusReasonUpdated")
			.withValidTo(LocalDate.of(2011, 2, 2));
	}

	public static AssetSearchRequest getAssetSearchRequest() {
		return AssetSearchRequest.create()
			.withAdditionalParameters(Map.of("key", "value"))
			.withAssetId("assetId")
			.withDescription("description")
			.withIssued(LocalDate.of(2010, 1, 1))
			.withPartyId("partyId")
			.withStatus(Status.ACTIVE)
			.withStatusReason("statusReason")
			.withType("type")
			.withValidTo(LocalDate.of(2010, 1, 1));
	}

	public static JsonSchemaEntity getJsonSchemaEntity() {
		return JsonSchemaEntity.create()
			.withCreated(OffsetDateTime.now())
			.withDescription("description")
			.withId("2281_person_schema_1.0.0")
			.withMunicipalityId("2281")
			.withName("person_schema")
			.withValue("{}")
			.withVersion("1.0");
	}

	public static JsonSchemaCreateRequest getJsonSchemaCreateRequest() {
		return JsonSchemaCreateRequest.create()
			.withDescription("description")
			.withName("person_schema")
			.withValue("{}")
			.withVersion("1.0");
	}
}
