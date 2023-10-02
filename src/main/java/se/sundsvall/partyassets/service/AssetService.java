package se.sundsvall.partyassets.service;

import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.partyassets.integration.db.model.PartyType.ENTERPRISE;
import static se.sundsvall.partyassets.integration.db.model.PartyType.PRIVATE;
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
import se.sundsvall.partyassets.integration.db.model.PartyType;
import se.sundsvall.partyassets.integration.party.PartyClient;
import se.sundsvall.partyassets.service.mapper.AssetMapper;

@Service
public class AssetService {

	private final AssetRepository repository;

	private final PartyClient partyClient;

	public AssetService(final AssetRepository repository, final PartyClient partyClient) {
		this.repository = repository;
		this.partyClient = partyClient;
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
		return repository.save(toEntity(request, calculatePartyType(request.getPartyId()))).getId();
	}

	private PartyType calculatePartyType(String partyId) {
		return partyClient.getLegalId(generated.se.sundsvall.party.PartyType.PRIVATE, partyId)
			.isPresent() ? PRIVATE : ENTERPRISE;
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
