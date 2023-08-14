package se.sundsvall.citizenassets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.citizenassets.TestFactory.getAssetCreateRequest;
import static se.sundsvall.citizenassets.TestFactory.getAssetEntity;
import static se.sundsvall.citizenassets.TestFactory.getAsssetUpdateRequest;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.citizenassets.api.model.AssetCreateRequest;
import se.sundsvall.citizenassets.api.model.AssetSearchRequest;
import se.sundsvall.citizenassets.api.model.AsssetUpdateRequest;
import se.sundsvall.citizenassets.integration.db.AssetRepository;
import se.sundsvall.citizenassets.integration.db.model.AssetEntity;
import se.sundsvall.citizenassets.integration.db.specification.AssetSpecification;
import se.sundsvall.citizenassets.service.mapper.Mapper;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

	@Mock
	private AssetRepository repository;

	@Mock
	private AssetSpecification assetSpecification;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private Mapper mapper;

	@Mock
	private Specification<AssetEntity> mockSpecification;

	@InjectMocks
	private AssetService service;

	@Test
	void getAssets() {
		final var uuid = UUID.randomUUID();

		final var entity = getAssetEntity(uuid);

		when(assetSpecification.createAssetSpecification(any())).thenReturn(mockSpecification);
		when(repository.findAll(Mockito.<Specification<AssetEntity>>any())).thenReturn(List.of(entity));

		final var result = service.getAssets(AssetSearchRequest.builder().build());

		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.get(0)).usingRecursiveComparison().isEqualTo(entity);

		verify(repository).findAll(Mockito.<Specification<AssetEntity>>any());
		verify(mapper).toDto(any(AssetEntity.class));
	}

	@Test
	void createAsset() {
		final var uuid = UUID.randomUUID();
		final var assetRequest = getAssetCreateRequest(uuid);
		final var entity = getAssetEntity(uuid);

		when(repository.save(any(AssetEntity.class))).thenReturn(entity);

		final var result = service.createAsset(assetRequest);

		assertThat(result).isNotNull().isEqualTo(String.valueOf(uuid));

		verify(repository).save(any(AssetEntity.class));
		verify(mapper).toEntity(any(AssetCreateRequest.class));
	}

	@Test
	void createAssetWithExistingAssetId() {
		final var uuid = UUID.randomUUID();
		final var assetCreateRequest = getAssetCreateRequest(uuid);

		when(repository.save(any(AssetEntity.class))).thenThrow(new DataIntegrityViolationException(""));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.createAsset(assetCreateRequest))
			.withMessage("Asset already exists: Asset with assetId assetId already exists");

		verify(mapper).toEntity(any(AssetCreateRequest.class));
		verify(repository).save(any(AssetEntity.class));
	}

	@Test
	void deleteAsset() {
		final var uuid = UUID.randomUUID();
		service.deleteAsset(uuid);
		verify(repository).deleteById(any(UUID.class));
	}

	@Test
	void updateAsset() {
		final var uuid = UUID.randomUUID();

		final var asssetUpdateRequest = getAsssetUpdateRequest();
		final var entity = getAssetEntity(uuid);

		when(repository.findByAssetId(any())).thenReturn(java.util.Optional.of(entity));

		service.updateAsset(uuid, "assetId", asssetUpdateRequest);

		verify(repository).findByAssetId(any(String.class));
		verify(repository).save(any(AssetEntity.class));
		verify(mapper).updateEntity(any(AssetEntity.class), any(AsssetUpdateRequest.class));
	}

	@Test
	void updateAssetThatDoesntExists() {

		final var uuid = UUID.randomUUID();
		final var assetUpdaterequest = getAsssetUpdateRequest();

		when(repository.findByAssetId(any(String.class))).thenReturn(java.util.Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.updateAsset(uuid, "assetId", assetUpdaterequest))
			.withMessage("Asset not found: Asset with assetId assetId not found");

		verify(repository).findByAssetId(any(String.class));
		verify(mapper, never()).toDto(any(AssetEntity.class));
	}
}
