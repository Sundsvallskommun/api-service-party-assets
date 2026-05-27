package se.sundsvall.partyassets.service;

import generated.se.sundsvall.relation.Relation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.partyassets.api.model.AssetSearchRequest;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.PartyType;
import se.sundsvall.partyassets.integration.db.specification.AssetSpecification;
import se.sundsvall.partyassets.integration.party.PartyTypeProvider;
import se.sundsvall.partyassets.integration.relation.RelationClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.partyassets.TestFactory.getAssetCreateRequest;
import static se.sundsvall.partyassets.TestFactory.getAssetEntity;
import static se.sundsvall.partyassets.TestFactory.getAssetUpdateRequest;
import static se.sundsvall.partyassets.api.model.Status.ACTIVE;
import static se.sundsvall.partyassets.api.model.Status.DRAFT;
import static se.sundsvall.partyassets.api.model.Status.REPLACED;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private AssetRepository repositoryMock;

	@Mock
	private Specification<AssetEntity> specificationMock;

	@Mock
	private Specification<AssetEntity> specificationExcludingDraftAsssetsMock;

	@Mock
	private Specification<AssetEntity> combinedSpecificationMock;

	@Captor
	private ArgumentCaptor<AssetSearchRequest> searchRequestCaptor;

	@Mock
	private PartyTypeProvider partyTypeProviderMock;

	@Captor
	private ArgumentCaptor<AssetEntity> entityCaptor;

	@Mock
	private RelationClient relationClientMock;

	@Captor
	private ArgumentCaptor<Relation> relationCaptor;

	@InjectMocks
	private AssetService service;

	@Test
	void getAssets() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);
		final var request = new AssetSearchRequest();

		try (final var _ = mockStatic(AssetSpecification.class)) {
			when(AssetSpecification.createAssetSpecification(MUNICIPALITY_ID, request)).thenReturn(specificationMock);
			when(AssetSpecification.createAssetSpecificationExcludingDraftAsssets()).thenReturn(specificationExcludingDraftAsssetsMock);
			when(specificationMock.and(specificationExcludingDraftAsssetsMock)).thenReturn(combinedSpecificationMock);
			when(repositoryMock.findAll(combinedSpecificationMock)).thenReturn(List.of(entity));

			final var result = service.getAssets(MUNICIPALITY_ID, request);

			assertThat(result).isNotNull().hasSize(1);
			assertThat(result.getFirst()).usingRecursiveComparison().ignoringFields("jsonParameters").isEqualTo(entity);
		}

		verify(repositoryMock).findAll(combinedSpecificationMock);
	}

	@Test
	void getDraftAssets() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);
		final var request = new AssetSearchRequest();

		try (final var assetSpecificationStaticMock = mockStatic(AssetSpecification.class)) {
			when(AssetSpecification.createAssetSpecification(MUNICIPALITY_ID, request)).thenReturn(specificationMock);
			when(repositoryMock.findAll(specificationMock)).thenReturn(List.of(entity));

			final var result = service.getDraftAssets(MUNICIPALITY_ID, request);

			assertThat(result).isNotNull().hasSize(1);
			assertThat(result.getFirst()).usingRecursiveComparison().ignoringFields("jsonParameters").isEqualTo(entity);

			assetSpecificationStaticMock.verify(() -> AssetSpecification.createAssetSpecification(eq(MUNICIPALITY_ID), searchRequestCaptor.capture()));

			var searchRequest = searchRequestCaptor.getValue();
			assertThat(searchRequest.getStatus()).isEqualTo(DRAFT);
		}

		verify(repositoryMock).findAll(specificationMock);
	}

	@Test
	void getAsset() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);

		when(repositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		final var result = service.getAsset(MUNICIPALITY_ID, id);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(id);

		verify(repositoryMock).findByIdAndMunicipalityId(id, MUNICIPALITY_ID);
	}

	@Test
	void getAssetNotFound() {
		final var id = UUID.randomUUID().toString();

		when(repositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.getAsset(MUNICIPALITY_ID, id))
			.withMessage("Asset not found: Asset with id " + id + " not found for municipalityId " + MUNICIPALITY_ID);

		verify(repositoryMock).findByIdAndMunicipalityId(id, MUNICIPALITY_ID);
	}

	@Test
	void createAssetForPrivateCustomer() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);
		final var assetCreateRequest = getAssetCreateRequest(partyId);

		when(partyTypeProviderMock.calculatePartyType(MUNICIPALITY_ID, partyId)).thenReturn(PartyType.PRIVATE);
		when(repositoryMock.save(any(AssetEntity.class))).thenReturn(entity);

		final var result = service.createAsset(MUNICIPALITY_ID, assetCreateRequest, null);

		verify(partyTypeProviderMock).calculatePartyType(MUNICIPALITY_ID, partyId);
		verify(repositoryMock).existsByAssetIdAndMunicipalityId(assetCreateRequest.getAssetId(), MUNICIPALITY_ID);
		verify(repositoryMock).save(entityCaptor.capture());

		assertThat(entityCaptor.getValue().getPartyType()).isEqualTo(PartyType.PRIVATE);
		assertThat(result).isNotNull().isEqualTo(String.valueOf(id));
		verifyNoMoreInteractions(repositoryMock, partyTypeProviderMock, specificationMock, specificationExcludingDraftAsssetsMock, relationClientMock);
	}

	@Test
	void createAssetWithSourceReference() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);
		final var assetCreateRequest = getAssetCreateRequest(partyId);
		final var sourceReference = "LINK|1234;case;service;MY_NAMESPACE|";

		when(partyTypeProviderMock.calculatePartyType(MUNICIPALITY_ID, partyId)).thenReturn(PartyType.PRIVATE);
		when(repositoryMock.save(any(AssetEntity.class))).thenReturn(entity);

		final var result = service.createAsset(MUNICIPALITY_ID, assetCreateRequest, sourceReference);

		verify(partyTypeProviderMock).calculatePartyType(MUNICIPALITY_ID, partyId);
		verify(repositoryMock).existsByAssetIdAndMunicipalityId(assetCreateRequest.getAssetId(), MUNICIPALITY_ID);
		verify(repositoryMock).save(entityCaptor.capture());
		verify(relationClientMock).createRelation(eq(MUNICIPALITY_ID), relationCaptor.capture());

		assertThat(entityCaptor.getValue().getPartyType()).isEqualTo(PartyType.PRIVATE);
		assertThat(result).isNotNull().isEqualTo(String.valueOf(id));
		assertThat(relationCaptor.getValue()).isNotNull();
		assertThat(relationCaptor.getValue().getType()).isEqualTo("LINK");
		assertThat(relationCaptor.getValue().getSource().getResourceId()).isEqualTo("1234");
		assertThat(relationCaptor.getValue().getSource().getType()).isEqualTo("case");
		assertThat(relationCaptor.getValue().getSource().getService()).isEqualTo("service");
		assertThat(relationCaptor.getValue().getSource().getNamespace()).isEqualTo("MY_NAMESPACE");
		assertThat(relationCaptor.getValue().getTarget().getResourceId()).isEqualTo(String.valueOf(id));
		assertThat(relationCaptor.getValue().getTarget().getType()).isEqualTo("asset");
		assertThat(relationCaptor.getValue().getTarget().getService()).isEqualTo("partyassets");
		verifyNoMoreInteractions(repositoryMock, partyTypeProviderMock, specificationMock, specificationExcludingDraftAsssetsMock, relationClientMock);
	}

	@Test
	void createAssetWithSourceReferenceInInvalidFormat() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);
		final var assetCreateRequest = getAssetCreateRequest(partyId);
		final var sourceReference = "||1234;invalid-format;service;MY_NAMESPACE";

		when(partyTypeProviderMock.calculatePartyType(MUNICIPALITY_ID, partyId)).thenReturn(PartyType.PRIVATE);
		when(repositoryMock.save(any(AssetEntity.class))).thenReturn(entity);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.createAsset(MUNICIPALITY_ID, assetCreateRequest, sourceReference))
			.withMessage("Invalid source reference: Provided source reference '||1234;invalid-format;service;MY_NAMESPACE' is invalid. Expected format: '{relationType}|{sourceResourceId};{sourceType};{sourceService};{sourceNamespace}|'");

		verify(partyTypeProviderMock).calculatePartyType(MUNICIPALITY_ID, partyId);
		verify(repositoryMock).existsByAssetIdAndMunicipalityId(assetCreateRequest.getAssetId(), MUNICIPALITY_ID);
		verify(repositoryMock).save(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getPartyType()).isEqualTo(PartyType.PRIVATE);
		verifyNoMoreInteractions(repositoryMock, partyTypeProviderMock, specificationMock, specificationExcludingDraftAsssetsMock, relationClientMock);
	}

	@Test
	void createAssetWithSourceReferenceMissingType() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);
		final var assetCreateRequest = getAssetCreateRequest(partyId);
		final var sourceReference = "|1234;case;service;MY_NAMESPACE|";

		when(partyTypeProviderMock.calculatePartyType(MUNICIPALITY_ID, partyId)).thenReturn(PartyType.PRIVATE);
		when(repositoryMock.save(any(AssetEntity.class))).thenReturn(entity);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.createAsset(MUNICIPALITY_ID, assetCreateRequest, sourceReference))
			.withMessage("Invalid source reference: Provided source reference '|1234;case;service;MY_NAMESPACE|' is invalid. Expected format: '{relationType}|{sourceResourceId};{sourceType};{sourceService};{sourceNamespace}|'");

		verify(partyTypeProviderMock).calculatePartyType(MUNICIPALITY_ID, partyId);
		verify(repositoryMock).existsByAssetIdAndMunicipalityId(assetCreateRequest.getAssetId(), MUNICIPALITY_ID);
		verify(repositoryMock).save(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getPartyType()).isEqualTo(PartyType.PRIVATE);
		verifyNoMoreInteractions(repositoryMock, partyTypeProviderMock, specificationMock, specificationExcludingDraftAsssetsMock, relationClientMock);
	}

	@Test
	void createAssetForEnterpriseCustomer() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);
		final var assetCreateRequest = getAssetCreateRequest(partyId);

		when(partyTypeProviderMock.calculatePartyType(MUNICIPALITY_ID, partyId)).thenReturn(PartyType.ENTERPRISE);
		when(repositoryMock.save(any(AssetEntity.class))).thenReturn(entity);

		final var result = service.createAsset(MUNICIPALITY_ID, assetCreateRequest, null);

		verify(partyTypeProviderMock).calculatePartyType(MUNICIPALITY_ID, partyId);
		verify(repositoryMock).existsByAssetIdAndMunicipalityId(assetCreateRequest.getAssetId(), MUNICIPALITY_ID);
		verify(repositoryMock).save(entityCaptor.capture());

		assertThat(entityCaptor.getValue().getPartyType()).isEqualTo(PartyType.ENTERPRISE);
		assertThat(result).isNotNull().isEqualTo(String.valueOf(id));
		verifyNoMoreInteractions(repositoryMock, partyTypeProviderMock, specificationMock, specificationExcludingDraftAsssetsMock, relationClientMock);
	}

	@Test
	void createAssetWithExistingAssetId() {
		final var partyId = UUID.randomUUID().toString();
		final var assetCreateRequest = getAssetCreateRequest(partyId);

		when(repositoryMock.existsByAssetIdAndMunicipalityId(assetCreateRequest.getAssetId(), MUNICIPALITY_ID)).thenReturn(true);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.createAsset(MUNICIPALITY_ID, assetCreateRequest, null))
			.withMessage("Asset already exists: Asset with assetId assetId already exists");

		verify(repositoryMock).existsByAssetIdAndMunicipalityId(assetCreateRequest.getAssetId(), MUNICIPALITY_ID);
		verify(partyTypeProviderMock, never()).calculatePartyType(any(), any());
		verify(repositoryMock, never()).save(any(AssetEntity.class));
		verify(relationClientMock, never()).createRelation(any(), any());
	}

	@Test
	void createAssetWithNullAssetId() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId);
		final var assetCreateRequest = getAssetCreateRequest(partyId).withAssetId(null);

		when(partyTypeProviderMock.calculatePartyType(MUNICIPALITY_ID, partyId)).thenReturn(PartyType.PRIVATE);
		when(repositoryMock.save(any(AssetEntity.class))).thenReturn(entity);

		final var result = service.createAsset(MUNICIPALITY_ID, assetCreateRequest, null);

		verify(partyTypeProviderMock).calculatePartyType(MUNICIPALITY_ID, partyId);
		verify(repositoryMock, never()).existsByAssetIdAndMunicipalityId(any(), any());
		verify(repositoryMock).save(any(AssetEntity.class));
		verify(relationClientMock, never()).createRelation(any(), any());

		assertThat(result).isNotNull().isEqualTo(String.valueOf(id));
	}

	@Test
	void deleteAsset() {
		final var uuid = UUID.randomUUID().toString();

		when(repositoryMock.existsByIdAndMunicipalityId(uuid, MUNICIPALITY_ID)).thenReturn(true);

		service.deleteAsset(MUNICIPALITY_ID, uuid);
		verify(repositoryMock).existsByIdAndMunicipalityId(uuid, MUNICIPALITY_ID);
		verify(repositoryMock).deleteByIdAndMunicipalityId(uuid, MUNICIPALITY_ID);
	}

	@Test
	void deleteNonExistingAsset() {
		final var uuid = UUID.randomUUID().toString();

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.deleteAsset(MUNICIPALITY_ID, uuid))
			.withMessage("Asset not found: Asset with id " + uuid + " not found for municipalityId " + MUNICIPALITY_ID);
		verify(repositoryMock).existsByIdAndMunicipalityId(uuid, MUNICIPALITY_ID);
		verify(repositoryMock, never()).deleteByIdAndMunicipalityId(uuid, MUNICIPALITY_ID);
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

	@Test
	void copyAssetSetsOriginalToReplacedAndCreatesDraft() {
		final var originalId = UUID.randomUUID().toString();
		final var newId = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var original = getAssetEntity(originalId, partyId);

		when(repositoryMock.findByIdAndMunicipalityId(originalId, MUNICIPALITY_ID)).thenReturn(Optional.of(original));
		when(repositoryMock.save(any(AssetEntity.class))).thenAnswer(inv -> {
			final AssetEntity e = inv.getArgument(0);
			e.withId(newId);
			return e;
		});

		final var result = service.copyAsset(MUNICIPALITY_ID, originalId);

		verify(repositoryMock).save(entityCaptor.capture());
		final var savedEntity = entityCaptor.getValue();
		assertThat(savedEntity.getStatus()).isEqualTo(DRAFT);
		assertThat(savedEntity.getReplacesId()).isEqualTo(originalId);
		assertThat(result).isEqualTo(newId);
	}

	@Test
	void copyAssetFailsWhenNotActive() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId).withStatus(DRAFT);

		when(repositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.copyAsset(MUNICIPALITY_ID, id))
			.withMessage("Asset cannot be copied: Only ACTIVE assets can be copied, but asset " + id + " has status DRAFT");

		verify(repositoryMock, never()).save(any());
	}

	@Test
	void updateDraftAssetToActiveSetsOriginalToReplaced() {
		final var draftId = UUID.randomUUID().toString();
		final var originalId = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var draft = getAssetEntity(draftId, partyId)
			.withStatus(DRAFT)
			.withReplacesId(originalId)
			.withValidTo(java.time.LocalDate.now().plusDays(30));
		final var original = getAssetEntity(originalId, partyId).withStatus(ACTIVE);
		final var request = new se.sundsvall.partyassets.api.model.DraftAssetUpdateRequest().withStatus(ACTIVE);

		when(repositoryMock.findByIdAndMunicipalityId(draftId, MUNICIPALITY_ID)).thenReturn(Optional.of(draft));
		when(repositoryMock.findByIdAndMunicipalityId(originalId, MUNICIPALITY_ID)).thenReturn(Optional.of(original));

		service.updateAsset(MUNICIPALITY_ID, draftId, request);

		verify(repositoryMock, org.mockito.Mockito.times(2)).save(entityCaptor.capture());
		assertThat(entityCaptor.getAllValues()).anySatisfy(e -> assertThat(e.getStatus()).isEqualTo(REPLACED));
		assertThat(entityCaptor.getAllValues()).anySatisfy(e -> assertThat(e.getStatus()).isEqualTo(ACTIVE));
	}

	@Test
	void updateDraftAssetToActiveFailsWhenValidToIsInPast() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId)
			.withStatus(DRAFT)
			.withValidTo(java.time.LocalDate.now().minusDays(1));
		final var request = new se.sundsvall.partyassets.api.model.DraftAssetUpdateRequest().withStatus(ACTIVE);

		when(repositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.updateAsset(MUNICIPALITY_ID, id, request))
			.withMessage("Invalid validTo date: validTo must be in the future when activating an asset");

		verify(repositoryMock, never()).save(any());
	}

	@Test
	void updateDraftAssetToActiveWithNoReplacesIdDoesNotTriggerReplacement() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId)
			.withStatus(DRAFT)
			.withValidTo(java.time.LocalDate.now().plusDays(10));
		final var request = new se.sundsvall.partyassets.api.model.DraftAssetUpdateRequest().withStatus(ACTIVE);

		when(repositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		service.updateAsset(MUNICIPALITY_ID, id, request);

		verify(repositoryMock).findByIdAndMunicipalityId(id, MUNICIPALITY_ID);
		verify(repositoryMock).save(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getStatus()).isEqualTo(ACTIVE);
		verifyNoMoreInteractions(repositoryMock);
	}

	@Test
	void updateDraftAssetToActiveDoesNotReplaceAlreadyReplacedOriginal() {
		final var draftId = UUID.randomUUID().toString();
		final var originalId = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var draft = getAssetEntity(draftId, partyId)
			.withStatus(DRAFT)
			.withReplacesId(originalId)
			.withValidTo(java.time.LocalDate.now().plusDays(30));
		final var original = getAssetEntity(originalId, partyId).withStatus(REPLACED);
		final var request = new se.sundsvall.partyassets.api.model.DraftAssetUpdateRequest().withStatus(ACTIVE);

		when(repositoryMock.findByIdAndMunicipalityId(draftId, MUNICIPALITY_ID)).thenReturn(Optional.of(draft));
		when(repositoryMock.findByIdAndMunicipalityId(originalId, MUNICIPALITY_ID)).thenReturn(Optional.of(original));

		service.updateAsset(MUNICIPALITY_ID, draftId, request);

		verify(repositoryMock).save(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getStatus()).isEqualTo(ACTIVE);
		assertThat(original.getStatus()).isEqualTo(REPLACED);
	}

	@Test
	void updateDraftAsset() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId).withStatus(DRAFT);

		when(repositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		service.updateAsset(MUNICIPALITY_ID, id, new se.sundsvall.partyassets.api.model.DraftAssetUpdateRequest());

		verify(repositoryMock).findByIdAndMunicipalityId(id, MUNICIPALITY_ID);
		verify(repositoryMock).save(any(AssetEntity.class));
	}

	@Test
	void updateNonDraftAssetViaDraftEndpointThrowsBadRequest() {
		final var id = UUID.randomUUID().toString();
		final var partyId = UUID.randomUUID().toString();
		final var entity = getAssetEntity(id, partyId); // status ACTIVE

		when(repositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.updateAsset(MUNICIPALITY_ID, id, new se.sundsvall.partyassets.api.model.DraftAssetUpdateRequest()))
			.withMessage("Invalid asset status: Only DRAFT assets can be updated via this endpoint");

		verify(repositoryMock).findByIdAndMunicipalityId(id, MUNICIPALITY_ID);
		verify(repositoryMock, never()).save(any());
	}
}
