package se.sundsvall.partyassets.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.partyassets.api.model.Asset;
import se.sundsvall.partyassets.api.model.AssetCreateRequest;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.api.model.AssetUpdateRequest;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.party.PartyTypeProvider;
import se.sundsvall.partyassets.service.mapper.AssetMapper;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.partyassets.integration.db.specification.AssetSpecification.createAssetSpecification;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.toEntity;
import static se.sundsvall.partyassets.service.mapper.AssetMapper.updateEntity;

@Service
@Transactional
public class AssetService {

	private static final String ASSET_NOT_FOUND_TITLE = "Asset not found";
	private static final String ASSET_NOT_FOUND_DETAIL = "Asset with id %s not found for municipalityId %s";

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

	public Asset getAsset(final String municipalityId, final String id) {
		return repository.findByIdAndMunicipalityId(id, municipalityId)
			.map(AssetMapper::toAsset)
			.orElseThrow(() -> Problem.builder()
				.withStatus(NOT_FOUND)
				.withTitle(ASSET_NOT_FOUND_TITLE)
				.withDetail(ASSET_NOT_FOUND_DETAIL.formatted(id, municipalityId))
				.build());
	}

	public String createAsset(final String municipalityId, final AssetCreateRequest request) {
		if (isNotBlank(request.getAssetId()) && repository.existsByAssetIdAndMunicipalityId(request.getAssetId(), municipalityId)) {
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
				.withTitle(ASSET_NOT_FOUND_TITLE)
				.withDetail(ASSET_NOT_FOUND_DETAIL.formatted(id, municipalityId))
				.build();
		}

		repository.deleteByIdAndMunicipalityId(id, municipalityId);
	}

	public void updateAsset(final String municipalityId, final String id, final AssetUpdateRequest request) {

		final var old = repository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> Problem.builder()
				.withStatus(NOT_FOUND)
				.withTitle(ASSET_NOT_FOUND_TITLE)
				.withDetail(ASSET_NOT_FOUND_DETAIL.formatted(id, municipalityId))
				.build());

		repository.save(updateEntity(old, request));
	}
}
