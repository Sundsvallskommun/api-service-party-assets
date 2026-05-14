package se.sundsvall.partyassets.scheduler;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetExpirationWorkerTest {

	private static final String ASSET_ID = "some-asset-id";
	private static final LocalDate VALID_TO = LocalDate.of(2026, 1, 1);

	@Mock
	private AssetRepository assetRepositoryMock;

	@InjectMocks
	private AssetExpirationWorker worker;

	@Test
	void expireAssets_withNoAssets() {
		when(assetRepositoryMock.findByStatusAndValidToBefore(Status.ACTIVE, LocalDate.now())).thenReturn(List.of());

		worker.expireAssets();

		verify(assetRepositoryMock).findByStatusAndValidToBefore(Status.ACTIVE, LocalDate.now());
		verifyNoMoreInteractions(assetRepositoryMock);
	}

	@Test
	void expireAssets_setsStatusAndSavesEachAsset() {
		final var asset1 = AssetEntity.create().withId("asset-1").withStatus(Status.ACTIVE).withValidTo(VALID_TO);
		final var asset2 = AssetEntity.create().withId("asset-2").withStatus(Status.ACTIVE).withValidTo(VALID_TO);
		when(assetRepositoryMock.findByStatusAndValidToBefore(Status.ACTIVE, LocalDate.now())).thenReturn(List.of(asset1, asset2));

		worker.expireAssets();

		assertThat(asset1.getStatus()).isEqualTo(Status.EXPIRED);
		assertThat(asset2.getStatus()).isEqualTo(Status.EXPIRED);
		verify(assetRepositoryMock).save(asset1);
		verify(assetRepositoryMock).save(asset2);
	}

	@Test
	void expire_setsStatusAndSaves() {
		final var asset = AssetEntity.create()
			.withId(ASSET_ID)
			.withStatus(Status.ACTIVE)
			.withValidTo(VALID_TO);

		worker.expire(asset);

		assertThat(asset.getStatus()).isEqualTo(Status.EXPIRED);
		verify(assetRepositoryMock).save(asset);
	}
}
