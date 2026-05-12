package se.sundsvall.partyassets.scheduler;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetExpirationJobTest {

	@Mock
	private AssetRepository assetRepositoryMock;

	@Mock
	private AssetExpirationWorker assetExpirationWorkerMock;

	private AssetExpirationJob job;

	@BeforeEach
	void setUp() {
		job = new AssetExpirationJob(assetRepositoryMock, assetExpirationWorkerMock);
	}

	@Test
	void runWithNoExpiredAssets() {
		// Arrange
		when(assetRepositoryMock.findByStatusAndValidToBefore(Status.ACTIVE, LocalDate.now())).thenReturn(List.of());

		// Act
		job.run();

		// Assert
		verify(assetRepositoryMock).findByStatusAndValidToBefore(Status.ACTIVE, LocalDate.now());
		verifyNoInteractions(assetExpirationWorkerMock);
	}

	@Test
	void runExpiresExpiredAssets() {
		// Arrange
		final var asset = AssetEntity.create().withId("asset-1");
		when(assetRepositoryMock.findByStatusAndValidToBefore(Status.ACTIVE, LocalDate.now())).thenReturn(List.of(asset));

		// Act
		job.run();

		// Assert
		verify(assetExpirationWorkerMock).expire(asset);
	}

	@Test
	void runExpiresMultipleExpiredAssets() {
		// Arrange
		final var asset1 = AssetEntity.create().withId("asset-1");
		final var asset2 = AssetEntity.create().withId("asset-2");
		when(assetRepositoryMock.findByStatusAndValidToBefore(Status.ACTIVE, LocalDate.now())).thenReturn(List.of(asset1, asset2));

		// Act
		job.run();

		// Assert
		verify(assetExpirationWorkerMock).expire(asset1);
		verify(assetExpirationWorkerMock).expire(asset2);
	}
}
