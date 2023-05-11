package se.sundsvall.citizenassets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.citizenassets.TestFactory.getAssetEntity;
import static se.sundsvall.citizenassets.TestFactory.getAssetRequest;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.citizenassets.api.model.AssetRequest;
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
    void getAsset() {
        var uuid = UUID.randomUUID();
        var entity = getAssetEntity(uuid);

        when(repository.findById(any())).thenReturn(java.util.Optional.of(entity));

        var result = service.getAsset(uuid);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(entity);

        verify(repository, times(1)).findById(any(UUID.class));
        verify(mapper, times(1)).toDto(any(AssetEntity.class));
    }

    @Test
    void getAsset_NothingFound(){
        var uuid = UUID.randomUUID();

        when(repository.findById(any())).thenReturn(java.util.Optional.empty());
        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(()->service.getAsset(uuid))
            .withMessage("Asset not found: Asset with id "+ uuid+" not found");

        verify(repository, times(1)).findById(any(UUID.class));
        verify(mapper, times(0)).toDto(any(AssetEntity.class));
    }


    @Test
    void getAssets() {
        var uuid = UUID.randomUUID();

        var entity = getAssetEntity(uuid);

        when(assetSpecification.createAssetSpecification(any())).thenReturn(mockSpecification);
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(entity));

        var result = service.getAssets(AssetRequest.builder().build());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).usingRecursiveComparison().isEqualTo(entity);

        verify(repository, times(1)).findAll(any(Specification.class));
        verify(mapper, times(1)).toDto(any(AssetEntity.class));
    }



    @Test
    void createAsset(){
        var uuid = UUID.randomUUID();
        var assetRequest = getAssetRequest(uuid);
        var entity = getAssetEntity(uuid);

        when(repository.save(any(AssetEntity.class))).thenReturn(entity);


        var result = service.createAsset(assetRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(String.valueOf(uuid));

        verify(repository, times(1)).save(any(AssetEntity.class));
        verify(mapper, times(1)).toEntity(any(AssetRequest.class));

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

        var assetRequest = getAssetRequest(uuid);
        var entity = getAssetEntity(uuid);

        when(repository.findById(any())).thenReturn(java.util.Optional.of(entity));

       service.updateAsset(uuid, assetRequest);


        verify(repository, times(1)).save(any(AssetEntity.class));
        verify(mapper, times(1)).updateEntity(any(AssetEntity.class), any(AssetRequest.class));
    }

    @Test
    void updateAssetThatDoesntExists(){

        var uuid = UUID.randomUUID();

        when(repository.findById(any())).thenReturn(java.util.Optional.empty());
        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(()->service.updateAsset(uuid, getAssetRequest(uuid)))
            .withMessage("Asset not found: Asset with id "+ uuid+" not found");

        verify(repository, times(1)).findById(any(UUID.class));
        verify(mapper, times(0)).toDto(any(AssetEntity.class));
    }

}