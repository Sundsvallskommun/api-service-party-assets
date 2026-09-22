package se.sundsvall.partyassets.scheduler;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.AssetRevisionRepository;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;
import se.sundsvall.partyassets.integration.db.model.AssetRevisionEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetExpirationWorkerTest {

	private static final String ASSET_ID = "some-asset-id";
	private static final LocalDate VALID_TO = LocalDate.of(2026, 1, 1);

	@Mock
	private AssetRepository assetRepositoryMock;

	@Mock
	private AssetRevisionRepository assetRevisionRepositoryMock;

	@InjectMocks
	private AssetExpirationWorker worker;

	@Captor
	private ArgumentCaptor<AssetRevisionEntity> revisionCaptor;

	@Test
	void findExpirableAssetIds_withNoAssets() {
		when(assetRepositoryMock.findByStatusInAndValidToBefore(List.of(Status.ACTIVE, Status.TEMPORARY), LocalDate.now())).thenReturn(List.of());

		assertThat(worker.findExpirableAssetIds()).isEmpty();

		verify(assetRepositoryMock).findByStatusInAndValidToBefore(List.of(Status.ACTIVE, Status.TEMPORARY), LocalDate.now());
		verifyNoMoreInteractions(assetRepositoryMock, assetRevisionRepositoryMock);
	}

	@Test
	void findExpirableAssetIds_returnsIds() {
		when(assetRepositoryMock.findByStatusInAndValidToBefore(List.of(Status.ACTIVE, Status.TEMPORARY), LocalDate.now()))
			.thenReturn(List.of(() -> "asset-1", () -> "asset-2"));

		assertThat(worker.findExpirableAssetIds()).containsExactly("asset-1", "asset-2");

		verify(assetRepositoryMock).findByStatusInAndValidToBefore(List.of(Status.ACTIVE, Status.TEMPORARY), LocalDate.now());
		verifyNoMoreInteractions(assetRepositoryMock, assetRevisionRepositoryMock);
	}

	@Test
	void expire_recordsARevisionAndSetsStatus() {
		final var asset = AssetEntity.create()
			.withId(ASSET_ID)
			.withRevision(1)
			.withActor("previous.actor")
			.withStatus(Status.ACTIVE)
			.withValidTo(VALID_TO);
		when(assetRepositoryMock.findById(ASSET_ID)).thenReturn(Optional.of(asset));

		worker.expire(ASSET_ID);

		verify(assetRevisionRepositoryMock).save(revisionCaptor.capture());
		assertThat(revisionCaptor.getValue()).satisfies(revision -> {
			assertThat(revision.getAssetId()).isEqualTo(ASSET_ID);
			assertThat(revision.getRevision()).isEqualTo(1);
			assertThat(revision.getActor()).isEqualTo("previous.actor");
			assertThat(revision.getStatus()).isEqualTo("ACTIVE");
		});
		assertThat(asset.getStatus()).isEqualTo(Status.EXPIRED);
		assertThat(asset.getActor()).isNull();
		assertThat(asset.getRevision()).isEqualTo(2);
		verify(assetRepositoryMock).findById(ASSET_ID);
		verify(assetRepositoryMock).saveAndFlush(asset);
		verifyNoMoreInteractions(assetRepositoryMock, assetRevisionRepositoryMock);
	}

	@Test
	void expire_writesTheSnapshotOnlyAfterTheAssetUpdateHasFlushed() {
		final var asset = AssetEntity.create().withId(ASSET_ID).withRevision(1).withStatus(Status.ACTIVE).withValidTo(VALID_TO);
		when(assetRepositoryMock.findById(ASSET_ID)).thenReturn(Optional.of(asset));

		worker.expire(ASSET_ID);

		final var inOrder = inOrder(assetRepositoryMock, assetRevisionRepositoryMock);
		inOrder.verify(assetRepositoryMock).saveAndFlush(asset);
		inOrder.verify(assetRevisionRepositoryMock).save(any(AssetRevisionEntity.class));
	}

	@Test
	void expire_writesNoSnapshotWhenAnotherWriterAlreadyChangedTheAsset() {
		final var asset = AssetEntity.create().withId(ASSET_ID).withRevision(1).withStatus(Status.ACTIVE).withValidTo(VALID_TO);
		when(assetRepositoryMock.findById(ASSET_ID)).thenReturn(Optional.of(asset));
		when(assetRepositoryMock.saveAndFlush(asset)).thenThrow(new OptimisticLockingFailureException("conflict"));

		assertThatExceptionOfType(OptimisticLockingFailureException.class).isThrownBy(() -> worker.expire(ASSET_ID));

		verifyNoInteractions(assetRevisionRepositoryMock);
	}

	@Test
	void expire_skipsAnAssetThatNoLongerExists() {
		when(assetRepositoryMock.findById(ASSET_ID)).thenReturn(Optional.empty());

		worker.expire(ASSET_ID);

		verify(assetRepositoryMock).findById(ASSET_ID);
		verifyNoMoreInteractions(assetRepositoryMock);
		verifyNoInteractions(assetRevisionRepositoryMock);
	}

	@Test
	void expire_skipsAnAssetWhoseValidToWasExtended() {
		final var asset = AssetEntity.create().withId(ASSET_ID).withRevision(1).withStatus(Status.ACTIVE).withValidTo(LocalDate.now().plusYears(1));
		when(assetRepositoryMock.findById(ASSET_ID)).thenReturn(Optional.of(asset));

		worker.expire(ASSET_ID);

		assertThat(asset.getStatus()).isEqualTo(Status.ACTIVE);
		verify(assetRepositoryMock).findById(ASSET_ID);
		verifyNoMoreInteractions(assetRepositoryMock);
		verifyNoInteractions(assetRevisionRepositoryMock);
	}

	@Test
	void expire_skipsAnAssetWhoseStatusChanged() {
		final var asset = AssetEntity.create().withId(ASSET_ID).withStatus(Status.BLOCKED).withValidTo(VALID_TO);
		when(assetRepositoryMock.findById(ASSET_ID)).thenReturn(Optional.of(asset));

		worker.expire(ASSET_ID);

		assertThat(asset.getStatus()).isEqualTo(Status.BLOCKED);
		verify(assetRepositoryMock).findById(ASSET_ID);
		verifyNoMoreInteractions(assetRepositoryMock);
		verifyNoInteractions(assetRevisionRepositoryMock);
	}
}
