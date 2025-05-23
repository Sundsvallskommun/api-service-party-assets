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
import se.sundsvall.partyassets.integration.party.PartyTypeProvider;
import se.sundsvall.partyassets.service.mapper.AssetMapper;

@Service
public class AssetService {

	private final AssetRepository repository;

	private final PartyTypeProvider partyTypeProvider;

	public AssetService(final AssetRepository repository, final PartyTypeProvider partyTypeProvider) {
		this.repository = repository;
		this.partyTypeProvider = partyTypeProvider;
	}

	public List<Asset> getAssets(final String municipalityId, final AssetSearchRequest request) {
		return repository.findAll(createAssetSpecification(municipalityId, request))
			.stream()
			.map(AssetMapper::toAsset)
			.toList();
	}

	public String createAsset(final String municipalityId, final AssetCreateRequest request) {
		if (repository.existsByAssetIdAndMunicipalityId(request.getAssetId(), municipalityId)) {
			throw Problem.builder()
				.withStatus(CONFLICT)
				.withTitle("Asset already exists")
				.withDetail("Asset with assetId %s already exists".formatted(request.getAssetId()))
				.build();
		}
		return repository.save(toEntity(request, partyTypeProvider.calculatePartyType(municipalityId, request.getPartyId()), municipalityId)).getId();
	}

	public void deleteAsset(final String municipalityId, final String id) {
		if (!repository.existsByIdAndMunicipalityId(id, municipalityId)) {
			throw Problem.builder()
				.withStatus(NOT_FOUND)
				.withTitle("Asset not found")
				.withDetail("Asset with id %s not found for municipalityId %s".formatted(id, municipalityId))
				.build();
		}
		repository.deleteById(id);
	}

	public void updateAsset(final String municipalityId, final String id, final AssetUpdateRequest request) {

		final var old = repository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> Problem.builder()
				.withStatus(NOT_FOUND)
				.withTitle("Asset not found")
				.withDetail("Asset with id %s not found for municipalityId %s".formatted(id, municipalityId))
				.build());

		repository.save(updateEntity(old, request));
	}

}
