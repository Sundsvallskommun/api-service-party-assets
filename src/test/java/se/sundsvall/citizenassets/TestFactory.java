package se.sundsvall.citizenassets;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import se.sundsvall.citizenassets.api.model.Asset;
import se.sundsvall.citizenassets.api.model.AssetCreateRequest;
import se.sundsvall.citizenassets.api.model.AsssetUpdateRequest;
import se.sundsvall.citizenassets.api.model.Status;
import se.sundsvall.citizenassets.integration.db.model.AssetEntity;

public class TestFactory {


    public static AssetEntity getAssetEntity(UUID uuid) {
        return AssetEntity.builder()
            .withStatus(Status.ACTIVE)
            .withType("type")
            .withAssetId("assetId")
            .withId(uuid)
            .withPartyId(uuid)
            .withValidTo(LocalDate.of(2010, 1, 1))
            .withIssued(LocalDate.of(2010, 1, 1))
            .withCaseReferenceIds(List.of("caseReferenceId"))
            .withAdditionalParameters(Map.of("key", "value"))
            .withDescription("description")
            .build();
    }

    public static Asset getAsset() {
        return Asset.builder()
            .withStatus(Status.ACTIVE)
            .withType("type")
            .withAssetId("assetId")
            .withPartyId(UUID.randomUUID())
            .withValidTo(LocalDate.of(2010, 1, 1))
            .withIssued(LocalDate.of(2010, 1, 1))
            .withCaseReferenceIds(List.of("caseReferenceId"))
            .withAdditionalParameters(Map.of("key", "value"))
            .withDescription("description")
            .build();
    }

    public static AssetCreateRequest getAssetCreateRequest(UUID uuid) {
        return AssetCreateRequest.builder()
            .withStatus(Status.ACTIVE)
            .withType("type")
            .withPartyId(String.valueOf(uuid))
            .withAssetId("assetId")
            .withValidTo(LocalDate.of(2010, 1, 1))
            .withIssued(LocalDate.of(2010, 1, 1))
            .withCaseReferenceIds(List.of("caseReferenceId"))
            .withAdditionalParameters(Map.of("key", "value"))
            .withDescription("description")
            .build();
    }


    public static AsssetUpdateRequest getAsssetUpdateRequest() {
        return AsssetUpdateRequest.builder()
            .withStatus(Status.ACTIVE)
            .withValidTo(LocalDate.of(2010, 1, 1))
            .withCaseReferenceIds(List.of("caseReferenceId"))
            .withAdditionalParameters(Map.of("key", "value"))
            .build();
    }
}
