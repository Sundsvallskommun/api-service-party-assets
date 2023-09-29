package se.sundsvall.partyassets.service;

import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.partyassets.integration.db.specification.AssetSpecification.createAssetSpecification;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.toEntity;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.updateEntity;

import java.util.List;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.partyassets.api.model.Asset;
import se.sundsvall.partyassets.api.model.AssetCreateRequest;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.api.model.AssetUpdateRequest;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.service.mapper.AssetMapper;

@Service
public class AssetService {

	private final AssetRepository repository;

	public AssetService(final AssetRepository repository) {
		this.repository = repository;
	}

	public List<Asset> getAssets(AssetSearchRequest request) {
		return repository.findAll(createAssetSpecification(request))
			.stream()
			.map(AssetMapper::toAsset)
			.toList();
	}

	public String createAsset(final AssetCreateRequest request) {
		if (repository.existsByAssetId(request.getAssetId())) {
			throw Problem.builder()
				.withStatus(CONFLICT)
				.withTitle("Asset already exists")
				.withDetail("Asset with assetId %s already exists".formatted(request.getAssetId()))
				.build();
		}
		return repository.save(toEntity(request)).getId();
	}

	public void deleteAsset(final String id) {
		repository.deleteById(id);
	}

	public void updateAsset(final String id, final AssetUpdateRequest request) {

		final var old = repository.findById(id)
			.orElseThrow(() -> Problem.builder()
				.withStatus(NOT_FOUND)
				.withTitle("Asset not found")
				.withDetail("Asset with id %s not found".formatted(id))
				.build());
		repository.save(updateEntity(old, request));
	}
}
