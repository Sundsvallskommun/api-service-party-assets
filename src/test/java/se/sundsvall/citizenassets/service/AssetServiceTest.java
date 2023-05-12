package se.sundsvall.citizenassets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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
    AssetRepository repository;

    @Mock
    AssetSpecification assetSpecification;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    Mapper mapper;

    @Mock
    Specification<AssetEntity> mockSpecification;

    @InjectMocks
    AssetService service;


    @Test
    void getAssets() {
        var uuid = UUID.randomUUID();

        var entity = getAssetEntity(uuid);

        when(assetSpecification.createAssetSpecification(any())).thenReturn(mockSpecification);
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(entity));

        var result = service.getAssets(AssetSearchRequest.builder().build());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).usingRecursiveComparison().isEqualTo(entity);

        verify(repository, times(1)).findAll(any(Specification.class));
        verify(mapper, times(1)).toDto(any(AssetEntity.class));
    }



    @Test
    void createAsset(){
        var uuid = UUID.randomUUID();
        var assetRequest = getAssetCreateRequest(uuid);
        var entity = getAssetEntity(uuid);

        when(repository.save(any(AssetEntity.class))).thenReturn(entity);


        var result = service.createAsset(assetRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(String.valueOf(uuid));

        verify(repository, times(1)).save(any(AssetEntity.class));
        verify(mapper, times(1)).toEntity(any(AssetCreateRequest.class));

    }

    @Test
    void createAssetWithExistingAssetId() {
        var uuid = UUID.randomUUID();
        var assetCreateRequest = getAssetCreateRequest(uuid);

        when(repository.save(any(AssetEntity.class))).thenThrow(new DataIntegrityViolationException(""));

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(()->service.createAsset(assetCreateRequest))
            .withMessage("Asset already exists: Asset with assetId assetId already exists");

        verify(mapper, times(1)).toEntity(any(AssetCreateRequest.class));
        verify(repository, times(1)).save(any(AssetEntity.class));

    }

    @Test
    void deleteAsset(){
        var uuid = UUID.randomUUID();
        service.deleteAsset(uuid);
        verify(repository, times(1)).deleteById(any(UUID.class));
    }

    @Test
    void updateAsset(){
        var uuid = UUID.randomUUID();

        var asssetUpdateRequest = getAsssetUpdateRequest();
        var entity = getAssetEntity(uuid);

        when(repository.findByAssetId(any())).thenReturn(java.util.Optional.of(entity));

       service.updateAsset(uuid, "assetId",asssetUpdateRequest);


       verify(repository, times(1)).findByAssetId(any(String.class));
        verify(repository, times(1)).save(any(AssetEntity.class));
        verify(mapper, times(1)).updateEntity(any(AssetEntity.class), any(AsssetUpdateRequest.class));
    }

    @Test
    void updateAssetThatDoesntExists(){

        var uuid = UUID.randomUUID();

        when(repository.findByAssetId(any(String.class))).thenReturn(java.util.Optional.empty());
        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(()->service.updateAsset(uuid, "assetId",getAsssetUpdateRequest()))
            .withMessage("Asset not found: Asset with assetId assetId not found");

        verify(repository, times(1)).findByAssetId(any(String.class));
        verify(mapper, times(0)).toDto(any(AssetEntity.class));
    }

}