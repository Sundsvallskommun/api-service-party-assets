package se.sundsvall.partyassets.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.partyassets.TestFactory;
import se.sundsvall.partyassets.api.model.AssetUpdateRequest;

class AssetMapperTest {

	@Test
	void toAsset() {
		final var entity = TestFactory.getAssetEntity(UUID.randomUUID().toString(), UUID.randomUUID().toString());
		final var asset = AssetMapper.toAsset(entity);

		assertThat(asset.getAdditionalParameters()).isEqualTo(entity.getAdditionalParameters());
		assertThat(asset.getAssetId()).isEqualTo(entity.getAssetId());
		assertThat(asset.getCaseReferenceIds()).isEqualTo(entity.getCaseReferenceIds());
		assertThat(asset.getDescription()).isEqualTo(entity.getDescription());
		assertThat(asset.getId()).isEqualTo(entity.getId());
		assertThat(asset.getIssued()).isEqualTo(entity.getIssued());
		assertThat(asset.getPartyId()).isEqualTo(entity.getPartyId());
		assertThat(asset.getStatus()).isEqualTo(entity.getStatus());
		assertThat(asset.getStatusReason()).isEqualTo(entity.getStatusReason());
		assertThat(asset.getType()).isEqualTo(entity.getType());
		assertThat(asset.getValidTo()).isEqualTo(entity.getValidTo());
	}

	@Test
	void toEntity() {
		final var request = TestFactory.getAssetCreateRequest(UUID.randomUUID().toString());
		final var entity = AssetMapper.toEntity(request);

		assertThat(entity.getAdditionalParameters()).isEqualTo(request.getAdditionalParameters());
		assertThat(entity.getAssetId()).isEqualTo(request.getAssetId());
		assertThat(entity.getCaseReferenceIds()).isEqualTo(request.getCaseReferenceIds());
		assertThat(entity.getDescription()).isEqualTo(request.getDescription());
		assertThat(entity.getIssued()).isEqualTo(request.getIssued());
		assertThat(entity.getPartyId()).isEqualTo(request.getPartyId());
		assertThat(entity.getStatus()).isEqualTo(request.getStatus());
		assertThat(entity.getStatusReason()).isEqualTo(request.getStatusReason());
		assertThat(entity.getType()).isEqualTo(request.getType());
		assertThat(entity.getValidTo()).isEqualTo(request.getValidTo());
		assertThat(entity.getId()).isNull();
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getUpdated()).isNull();
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
		assertThat(entity.getCaseReferenceIds()).isEqualTo(request.getCaseReferenceIds());
		assertThat(entity.getStatus()).isEqualTo(request.getStatus());
		assertThat(entity.getStatusReason()).isEqualTo(request.getStatusReason());
		assertThat(entity.getValidTo()).isEqualTo(request.getValidTo());

		assertThat(entity.getAssetId()).isEqualTo(original.getAssetId());
		assertThat(entity.getDescription()).isEqualTo(original.getDescription());
		assertThat(entity.getIssued()).isEqualTo(original.getIssued());
		assertThat(entity.getPartyId()).isEqualTo(original.getPartyId());
		assertThat(entity.getType()).isEqualTo(original.getType());
		assertThat(entity.getId()).isEqualTo(original.getId());
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
		assertThat(entity.getCaseReferenceIds()).isEqualTo(original.getCaseReferenceIds());
		assertThat(entity.getStatus()).isEqualTo(original.getStatus());
		assertThat(entity.getStatusReason()).isEqualTo(original.getStatusReason());
		assertThat(entity.getValidTo()).isEqualTo(original.getValidTo());
		assertThat(entity.getAssetId()).isEqualTo(original.getAssetId());
		assertThat(entity.getDescription()).isEqualTo(original.getDescription());
		assertThat(entity.getIssued()).isEqualTo(original.getIssued());
		assertThat(entity.getPartyId()).isEqualTo(original.getPartyId());
		assertThat(entity.getType()).isEqualTo(original.getType());
		assertThat(entity.getId()).isEqualTo(original.getId());
	}
}
