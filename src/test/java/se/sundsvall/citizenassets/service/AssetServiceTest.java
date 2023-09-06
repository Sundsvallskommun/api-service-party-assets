package se.sundsvall.citizenassets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.citizenassets.TestFactory.getAssetCreateRequest;
import static se.sundsvall.citizenassets.TestFactory.getAssetEntity;
import static se.sundsvall.citizenassets.TestFactory.getAssetUpdateRequest;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.citizenassets.api.model.AssetSearchRequest;
import se.sundsvall.citizenassets.integration.db.AssetRepository;
import se.sundsvall.citizenassets.integration.db.model.AssetEntity;
import se.sundsvall.citizenassets.integration.db.specification.AssetSpecification;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

	@Mock
	private AssetRepository repositoryMock;

	@Mock
	private Specification<AssetEntity> specificationMock;

	@InjectMocks
	private AssetService service;

	@Test
	void getAssets() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);

		try (MockedStatic<AssetSpecification> assetSpecificationMock = mockStatic(AssetSpecification.class)) {
			when(AssetSpecification.createAssetSpecification(any())).thenReturn(specificationMock);
			when(repositoryMock.findAll(Mockito.<Specification<AssetEntity>>any())).thenReturn(List.of(entity));

			final var result = service.getAssets(AssetSearchRequest.create());

			assertThat(result).isNotNull().hasSize(1);
			assertThat(result.get(0)).usingRecursiveComparison().isEqualTo(entity);

		} ;

		verify(repositoryMock).findAll(Mockito.<Specification<AssetEntity>>any());
	}

	@Test
	void createAsset() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);
		final var assetRequest = getAssetCreateRequest(partyId);

		when(repositoryMock.save(any(AssetEntity.class))).thenReturn(entity);

		final var result = service.createAsset(assetRequest);

		assertThat(result).isNotNull().isEqualTo(String.valueOf(id));

		verify(repositoryMock).save(any(AssetEntity.class));
	}

	@Test
	void createAssetWithExistingAssetId() {
		final var partyId = UUID.randomUUID().toString();
		final var assetCreateRequest = getAssetCreateRequest(partyId);

		when(repositoryMock.save(any(AssetEntity.class))).thenThrow(new DataIntegrityViolationException(""));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.createAsset(assetCreateRequest))
			.withMessage("Asset already exists: Asset with assetId assetId already exists");

		verify(repositoryMock).save(any(AssetEntity.class));
	}

	@Test
	void deleteAsset() {
		final var uuid = UUID.randomUUID().toString();
		service.deleteAsset(uuid);
		verify(repositoryMock).deleteById(uuid);
	}

	@Test
	void updateAsset() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);
		final var asssetUpdateRequest = getAssetUpdateRequest();

		when(repositoryMock.findById(any())).thenReturn(java.util.Optional.of(entity));

		service.updateAsset(id, asssetUpdateRequest);

		verify(repositoryMock).findById(id);
		verify(repositoryMock).save(any(AssetEntity.class));
	}

	@Test
	void updateNonExistingAsset() {

		final var uuid = UUID.randomUUID().toString();
		final var assetUpdaterequest = getAssetUpdateRequest();

		when(repositoryMock.findById(any(String.class))).thenReturn(java.util.Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.updateAsset(uuid, assetUpdaterequest))
			.withMessage("Asset not found: Asset with id " + uuid + " not found");

		verify(repositoryMock).findById(any(String.class));
		verify(repositoryMock, never()).save(any());
	}
}
