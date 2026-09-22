package se.sundsvall.partyassets.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.partyassets.api.model.AssetRevision;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.AssetRevisionRepository;
import se.sundsvall.partyassets.integration.db.model.AssetRevisionEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.partyassets.TestFactory.getAssetEntity;

@ExtendWith(MockitoExtension.class)
class AssetRevisionServiceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private AssetRepository assetRepositoryMock;

	@Mock
	private AssetRevisionRepository assetRevisionRepositoryMock;

	@InjectMocks
	private AssetRevisionService service;

	private static AssetRevisionEntity revision(final String assetId, final int number, final String actor) {
		return AssetRevisionEntity.create()
			.withAssetId(assetId)
			.withRevision(number)
			.withActor(actor)
			.withRecordedAt(OffsetDateTime.now().minusDays(number))
			.withStatus("ACTIVE");
	}

	@Test
	void getRevisionsPutsTheCurrentRevisionFirst() {
		final var id = UUID.randomUUID().toString();
		final var asset = getAssetEntity(id, UUID.randomUUID().toString()).withRevision(2).withActor("current.actor");

		when(assetRepositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.of(asset));
		when(assetRevisionRepositoryMock.findByAssetIdOrderByRevisionDesc(id))
			.thenReturn(List.of(revision(id, 1, "second.actor"), revision(id, 0, null)));

		final var result = service.getRevisions(MUNICIPALITY_ID, id);

		assertThat(result)
			.extracting(AssetRevision::getRevision, AssetRevision::getActor)
			.containsExactly(tuple(2, "current.actor"), tuple(1, "second.actor"), tuple(0, null));
	}

	@Test
	void getRevisionsForAnAssetThatWasNeverChanged() {
		final var id = UUID.randomUUID().toString();
		final var created = OffsetDateTime.now().minusDays(3);
		final var asset = getAssetEntity(id, UUID.randomUUID().toString()).withRevision(0).withCreated(created).withUpdated(null);

		when(assetRepositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.of(asset));
		when(assetRevisionRepositoryMock.findByAssetIdOrderByRevisionDesc(id)).thenReturn(List.of());

		final var result = service.getRevisions(MUNICIPALITY_ID, id);

		// updated is null until the first change, so created is the only honest answer for recordedAt.
		assertThat(result).singleElement().satisfies(revision -> {
			assertThat(revision.getRevision()).isZero();
			assertThat(revision.getRecordedAt()).isEqualTo(created);
		});
	}

	@Test
	void getRevisionsForANonExistingAsset() {
		final var id = UUID.randomUUID().toString();
		when(assetRepositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.getRevisions(MUNICIPALITY_ID, id))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(NOT_FOUND));

		verifyNoInteractions(assetRevisionRepositoryMock);
	}

	@Test
	void getRevisionReadsTheAssetRowForTheCurrentNumber() {
		final var id = UUID.randomUUID().toString();
		final var asset = getAssetEntity(id, UUID.randomUUID().toString()).withRevision(3);

		when(assetRepositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.of(asset));

		final var result = service.getRevision(MUNICIPALITY_ID, id, 3);

		assertThat(result.getRevision()).isEqualTo(3);
		assertThat(result.getId()).isEqualTo(id);
		// The current revision lives on the asset row, so the history table is never touched.
		verifyNoInteractions(assetRevisionRepositoryMock);
	}

	@Test
	void getRevisionReadsTheHistoryTableForAnOlderNumber() {
		final var id = UUID.randomUUID().toString();
		final var asset = getAssetEntity(id, UUID.randomUUID().toString()).withRevision(3);

		when(assetRepositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.of(asset));
		when(assetRevisionRepositoryMock.findByAssetIdAndRevision(id, 1)).thenReturn(Optional.of(revision(id, 1, "second.actor")));

		final var result = service.getRevision(MUNICIPALITY_ID, id, 1);

		assertThat(result.getRevision()).isEqualTo(1);
		assertThat(result.getActor()).isEqualTo("second.actor");
		verify(assetRevisionRepositoryMock).findByAssetIdAndRevision(id, 1);
		verifyNoMoreInteractions(assetRevisionRepositoryMock);
	}

	// A gap means a write path mutated without snapshotting. It has to read as a miss, not as a neighbouring revision.
	@Test
	void getRevisionForAGapInTheNumbering() {
		final var id = UUID.randomUUID().toString();
		final var asset = getAssetEntity(id, UUID.randomUUID().toString()).withRevision(3);

		when(assetRepositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.of(asset));
		when(assetRevisionRepositoryMock.findByAssetIdAndRevision(id, 1)).thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.getRevision(MUNICIPALITY_ID, id, 1))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(NOT_FOUND));
	}

	@Test
	void getRevisionForANonExistingAsset() {
		final var id = UUID.randomUUID().toString();
		when(assetRepositoryMock.findByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.getRevision(MUNICIPALITY_ID, id, 0))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(NOT_FOUND));

		verifyNoInteractions(assetRevisionRepositoryMock);
	}

	@Test
	void getRevisionForAnAssetEntityIsNotAffectedByTheRequestedMunicipality() {
		final var id = UUID.randomUUID().toString();
		when(assetRepositoryMock.findByIdAndMunicipalityId(id, "2260")).thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.getRevision("2260", id, 0))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(NOT_FOUND));
	}
}
