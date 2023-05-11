package se.sundsvall.citizenassets.service.mapper;


import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.stereotype.Component;

import se.sundsvall.citizenassets.api.model.Asset;
import se.sundsvall.citizenassets.api.model.AssetRequest;
import se.sundsvall.citizenassets.integration.db.model.AssetEntity;

@Component
public class Mapper {


    public Asset toDto(AssetEntity entity) {
        return
            Asset.builder()
                .withId(entity.getId())
                .withPartyId(entity.getPartyId())
                .withCaseReferenceIds(entity.getCaseReferenceIds())
                .withType(entity.getType())
                .withIssued(entity.getIssued())
                .withValidTo(entity.getValidTo())
                .withStatus(entity.getStatus())
                .withDescription(entity.getDescription())
                .withAdditionalParameters(entity.getAdditionalParameters())
                .build();
    }

    public AssetEntity toEntity(AssetRequest request) {
        return AssetEntity.builder()
            .withPartyId(request.getPartyId())
            .withCaseReferenceIds(request.getCaseReferenceIds())
            .withType(request.getType())
            .withIssued(request.getIssued())
            .withValidTo(request.getValidTo())
            .withStatus(request.getStatus())
            .withDescription(request.getDescription())
            .withAdditionalParameters(request.getAdditionalParameters())
            .build();
    }

    public AssetEntity updateEntity(AssetEntity old, AssetRequest request) {

        if (request.getDescription() != null) {
            old.setDescription(request.getDescription());
        }

        if (request.getAdditionalParameters() != null) {

            var newMap = new HashMap<>(request.getAdditionalParameters());
            newMap.putAll(old.getAdditionalParameters());
            old.setAdditionalParameters(newMap);
        }

        if (request.getCaseReferenceIds() != null) {

            var list = new ArrayList<>(request.getCaseReferenceIds());
            list.addAll(old.getCaseReferenceIds());
            old.setCaseReferenceIds(list);
        }

        if (request.getIssued() != null) {
            old.setIssued(request.getIssued());
        }

        if (request.getStatus() != null) {
            old.setStatus(request.getStatus());
        }

        if (request.getType() != null) {
            old.setType(request.getType());
        }

        if (request.getValidTo() != null) {
            old.setValidTo(request.getValidTo());
        }

        if (request.getDescription() != null) {
            old.setDescription(request.getDescription());
        }

        return old;
    }
}
