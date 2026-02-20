package se.sundsvall.partyassets.service.mapper;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import se.sundsvall.partyassets.TestFactory;
import se.sundsvall.partyassets.api.model.AssetUpdateRequest;
import se.sundsvall.partyassets.integration.db.model.PartyType;

import static org.assertj.core.api.Assertions.assertThat;

class AssetMapperTest {

	@Test
	void toAsset() {

		final var entity = TestFactory.getAssetEntity(UUID.randomUUID().toString(), UUID.randomUUID().toString());
		final var asset = AssetMapper.toAsset(entity);

		assertThat(asset)
			.usingRecursiveComparison()
			.ignoringFields("jsonParameters")
			.isEqualTo(entity);

		// Json params
		assertThat(asset.getJsonParameters()).hasSize(1);
		assertThat(entity.getJsonParameters()).hasSize(1);
		final var assetParam = asset.getJsonParameters().getFirst();
		final var entityParam = entity.getJsonParameters().getFirst();
		assertThat(assetParam.getKey()).isEqualTo(entityParam.getKey());
		assertThat(assetParam.getSchemaId()).isEqualTo(entityParam.getSchemaId());
		assertThat(assetParam.getValue()).hasToString(entityParam.getValue());
	}

	@Test
	void toEntity() {

		final var request = TestFactory.getAssetCreateRequest(UUID.randomUUID().toString());
		final var partyType = PartyType.PRIVATE;
		final var municipalityId = "2281";
		final var entity = AssetMapper.toEntity(request, partyType, municipalityId);

		assertThat(entity.getAdditionalParameters()).isEqualTo(request.getAdditionalParameters());
		assertThat(entity.getAssetId()).isEqualTo(request.getAssetId());
		assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(entity.getDescription()).isEqualTo(request.getDescription());
		assertThat(entity.getIssued()).isEqualTo(request.getIssued());
		assertThat(entity.getPartyId()).isEqualTo(request.getPartyId());
		assertThat(entity.getPartyType()).isEqualTo(partyType);
		assertThat(entity.getStatus()).isEqualTo(request.getStatus());
		assertThat(entity.getStatusReason()).isEqualTo(request.getStatusReason());
		assertThat(entity.getType()).isEqualTo(request.getType());
		assertThat(entity.getValidTo()).isEqualTo(request.getValidTo());
		assertThat(entity.getId()).isNull();
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getUpdated()).isNull();

		// Json params
		assertThat(request.getJsonParameters()).hasSize(1);
		assertThat(entity.getJsonParameters()).hasSize(1);
		final var requestJsonParam = request.getJsonParameters().getFirst();
		final var entityParam = entity.getJsonParameters().getFirst();
		assertThat(requestJsonParam.getKey()).isEqualTo(entityParam.getKey());
		assertThat(requestJsonParam.getSchemaId()).isEqualTo(entityParam.getSchemaId());
		assertThat(requestJsonParam.getValue()).hasToString(entityParam.getValue());
	}

	@Test
	void updateEntity() {

		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var original = TestFactory.getAssetEntity(id, partyId);
		final var entity = TestFactory.getAssetEntity(id, partyId);
		final var request = TestFactory.getAssetUpdateRequest();

		AssetMapper.updateEntity(entity, request);

		assertThat(entity.getAdditionalParameters()).isEqualTo(request.getAdditionalParameters());
		assertThat(entity.getStatus()).isEqualTo(request.getStatus());
		assertThat(entity.getStatusReason()).isEqualTo(request.getStatusReason());
		assertThat(entity.getValidTo()).isEqualTo(request.getValidTo());

		assertThat(entity.getAssetId()).isEqualTo(original.getAssetId());
		assertThat(entity.getDescription()).isEqualTo(original.getDescription());
		assertThat(entity.getIssued()).isEqualTo(original.getIssued());
		assertThat(entity.getPartyId()).isEqualTo(original.getPartyId());
		assertThat(entity.getType()).isEqualTo(original.getType());
		assertThat(entity.getId()).isEqualTo(original.getId());
		assertThat(entity.getMunicipalityId()).isEqualTo(original.getMunicipalityId());

		// Json params
		assertThat(request.getJsonParameters()).hasSize(1);
		assertThat(entity.getJsonParameters()).hasSize(1);
		final var requestJsonParam = request.getJsonParameters().getFirst();
		final var entityParam = entity.getJsonParameters().getFirst();
		assertThat(requestJsonParam.getKey()).isEqualTo(entityParam.getKey());
		assertThat(requestJsonParam.getSchemaId()).isEqualTo(entityParam.getSchemaId());
		assertThat(requestJsonParam.getValue()).hasToString(entityParam.getValue());
	}

	@Test
	void updateEntityWithEmptyValues() {

		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var original = TestFactory.getAssetEntity(id, partyId);
		final var entity = TestFactory.getAssetEntity(id, partyId);
		final var request = AssetUpdateRequest.create();

		AssetMapper.updateEntity(entity, request);

		assertThat(entity.getAdditionalParameters()).isEqualTo(original.getAdditionalParameters());
		assertThat(entity.getStatus()).isEqualTo(original.getStatus());
		assertThat(entity.getStatusReason()).isEqualTo(original.getStatusReason());
		assertThat(entity.getValidTo()).isEqualTo(original.getValidTo());
		assertThat(entity.getAssetId()).isEqualTo(original.getAssetId());
		assertThat(entity.getDescription()).isEqualTo(original.getDescription());
		assertThat(entity.getIssued()).isEqualTo(original.getIssued());
		assertThat(entity.getJsonParameters()).isEqualTo(original.getJsonParameters());
		assertThat(entity.getPartyId()).isEqualTo(original.getPartyId());
		assertThat(entity.getType()).isEqualTo(original.getType());
		assertThat(entity.getId()).isEqualTo(original.getId());
		assertThat(entity.getMunicipalityId()).isEqualTo(original.getMunicipalityId());
	}
}
