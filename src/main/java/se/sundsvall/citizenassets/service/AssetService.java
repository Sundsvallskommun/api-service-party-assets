package se.sundsvall.citizenassets.service;

import static org.zalando.problem.Status.NOT_FOUND;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.citizenassets.api.model.Asset;
import se.sundsvall.citizenassets.api.model.AssetRequest;
import se.sundsvall.citizenassets.integration.db.AssetRepository;
import se.sundsvall.citizenassets.integration.db.specification.AssetSpecification;
import se.sundsvall.citizenassets.service.mapper.Mapper;

@Service
public class AssetService {

    private final AssetRepository repository;
    private final Mapper mapper;

    private final AssetSpecification specification;
    public AssetService(AssetRepository repository, Mapper mapper, AssetSpecification specification) {
        this.repository = repository;
        this.mapper = mapper;
        this.specification = specification;
    }

    public Asset getAsset(UUID id) {
        return mapper.toDto(repository.findById(id)
                .orElseThrow(() -> Problem.builder()
                        .withStatus(NOT_FOUND)
                        .withTitle("Asset not found")
                        .withDetail("Asset with id %s not found".formatted(id))
                        .build()));
    }

    public List<Asset> getAssets( AssetRequest request) {
        return repository.findAll(specification.createAssetSpecification(request))
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public String createAsset(AssetRequest request) {
        var entity = mapper.toEntity(request);

        return String.valueOf(repository.save(entity).getId());
    }

    public void deleteAsset(UUID id) {
        repository.deleteById(id);
    }

    public void updateAsset(UUID id, AssetRequest request) {

       var old = repository.findById(id)
           .orElseThrow(() ->
               Problem.builder()
                   .withStatus(NOT_FOUND)
                   .withTitle("Asset not found")
                   .withDetail("Asset with id %s not found".formatted(id))
                   .build());
        repository.save(mapper.updateEntity(old, request));
    }
}
