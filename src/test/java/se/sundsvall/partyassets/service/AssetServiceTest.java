package se.sundsvall.partyassets.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.PartyType;
import se.sundsvall.partyassets.integration.db.specification.AssetSpecification;
import se.sundsvall.partyassets.integration.party.PartyTypeProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.partyassets.TestFactory.getAssetCreateRequest;
import static se.sundsvall.partyassets.TestFactory.getAssetEntity;
import static se.sundsvall.partyassets.TestFactory.getAssetUpdateRequest;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private AssetRepository repositoryMock;

	@Mock
	private Specification<AssetEntity> specificationMock;

	@Mock
	private PartyTypeProvider partyTypeProviderMock;

	@Captor
	private ArgumentCaptor<AssetEntity> entityCaptor;

	@InjectMocks
	private AssetService service;

	@Test
	void getAssets() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);
		final var request = new AssetSearchRequest();

		try (final MockedStatic<AssetSpecification> assetSpecificationMock = mockStatic(AssetSpecification.class)) {
			when(AssetSpecification.createAssetSpecification(MUNICIPALITY_ID, request)).thenReturn(specificationMock);
			when(repositoryMock.findAll(Mockito.<Specification<AssetEntity>>any())).thenReturn(List.of(entity));

			final var result = service.getAssets(MUNICIPALITY_ID, request);

			assertThat(result).isNotNull().hasSize(1);
			assertThat(result.getFirst()).usingRecursiveComparison().ignoringFields("jsonParameters").isEqualTo(entity);

		}

		verify(repositoryMock).findAll(Mockito.<Specification<AssetEntity>>any());
	}

	@Test
	void createAssetForPrivateCustomer() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);
		final var assetCreateRequest = getAssetCreateRequest(partyId);

		when(partyTypeProviderMock.calculatePartyType(MUNICIPALITY_ID, partyId)).thenReturn(PartyType.PRIVATE);
		when(repositoryMock.save(any(AssetEntity.class))).thenReturn(entity);

		final var result = service.createAsset(MUNICIPALITY_ID, assetCreateRequest);

		verify(partyTypeProviderMock).calculatePartyType(MUNICIPALITY_ID, partyId);
		verify(repositoryMock).existsByAssetIdAndMunicipalityId(assetCreateRequest.getAssetId(), MUNICIPALITY_ID);
		verify(repositoryMock).save(entityCaptor.capture());

		assertThat(entityCaptor.getValue().getPartyType()).isEqualTo(PartyType.PRIVATE);
		assertThat(result).isNotNull().isEqualTo(String.valueOf(id));
	}

	@Test
	void createAssetForEnterpriseCustomer() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);
		final var assetCreateRequest = getAssetCreateRequest(partyId);

		when(partyTypeProviderMock.calculatePartyType(MUNICIPALITY_ID, partyId)).thenReturn(PartyType.ENTERPRISE);
		when(repositoryMock.save(any(AssetEntity.class))).thenReturn(entity);

		final var result = service.createAsset(MUNICIPALITY_ID, assetCreateRequest);

		verify(partyTypeProviderMock).calculatePartyType(MUNICIPALITY_ID, partyId);
		verify(repositoryMock).existsByAssetIdAndMunicipalityId(assetCreateRequest.getAssetId(), MUNICIPALITY_ID);
		verify(repositoryMock).save(entityCaptor.capture());

		assertThat(entityCaptor.getValue().getPartyType()).isEqualTo(PartyType.ENTERPRISE);
		assertThat(result).isNotNull().isEqualTo(String.valueOf(id));
	}

	@Test
	void createAssetWithExistingAssetId() {
		final var partyId = UUID.randomUUID().toString();
		final var assetCreateRequest = getAssetCreateRequest(partyId);

		when(repositoryMock.existsByAssetIdAndMunicipalityId(assetCreateRequest.getAssetId(), MUNICIPALITY_ID)).thenReturn(true);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.createAsset(MUNICIPALITY_ID, assetCreateRequest))
			.withMessage("Asset already exists: Asset with assetId assetId already exists");

		verify(repositoryMock).existsByAssetIdAndMunicipalityId(assetCreateRequest.getAssetId(), MUNICIPALITY_ID);
		verify(partyTypeProviderMock, never()).calculatePartyType(any(), any());
		verify(repositoryMock, never()).save(any(AssetEntity.class));
	}

	@Test
	void deleteAsset() {
		final var uuid = UUID.randomUUID().toString();

		when(repositoryMock.existsByIdAndMunicipalityId(uuid, MUNICIPALITY_ID)).thenReturn(true);

		service.deleteAsset(MUNICIPALITY_ID, uuid);
		verify(repositoryMock).existsByIdAndMunicipalityId(uuid, MUNICIPALITY_ID);
		verify(repositoryMock).deleteById(uuid);
	}

	@Test
	void deleteNonExistingAsset() {
		final var uuid = UUID.randomUUID().toString();

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.deleteAsset(MUNICIPALITY_ID, uuid))
			.withMessage("Asset not found: Asset with id " + uuid + " not found for municipalityId " + MUNICIPALITY_ID);
		verify(repositoryMock).existsByIdAndMunicipalityId(uuid, MUNICIPALITY_ID);
		verify(repositoryMock, never()).deleteById(uuid);
	}

	@Test
	void updateAsset() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);
		final var asssetUpdateRequest = getAssetUpdateRequest();

		when(repositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		service.updateAsset(MUNICIPALITY_ID, id, asssetUpdateRequest);

		verify(repositoryMock).findByIdAndMunicipalityId(id, MUNICIPALITY_ID);
		verify(repositoryMock).save(any(AssetEntity.class));
	}

	@Test
	void updateNonExistingAsset() {

		final var uuid = UUID.randomUUID().toString();
		final var assetUpdaterequest = getAssetUpdateRequest();

		when(repositoryMock.findByIdAndMunicipalityId(uuid, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.updateAsset(MUNICIPALITY_ID, uuid, assetUpdaterequest))
			.withMessage("Asset not found: Asset with id " + uuid + " not found for municipalityId " + MUNICIPALITY_ID);

		verify(repositoryMock).findByIdAndMunicipalityId(uuid, MUNICIPALITY_ID);
		verify(repositoryMock, never()).save(any());
	}
}
