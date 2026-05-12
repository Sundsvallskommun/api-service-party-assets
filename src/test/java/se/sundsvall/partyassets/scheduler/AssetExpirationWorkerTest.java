package se.sundsvall.partyassets.scheduler;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssetExpirationWorkerTest {

	private static final String ASSET_ID = "some-asset-id";
	private static final LocalDate VALID_TO = LocalDate.of(2026, 1, 1);

	@Mock
	private AssetRepository assetRepositoryMock;

	private AssetExpirationWorker worker;

	@BeforeEach
	void setUp() {
		worker = new AssetExpirationWorker(assetRepositoryMock);
	}

	@Test
	void expireSetsStatusAndSaves() {
		// Arrange
		final var asset = AssetEntity.create()
			.withId(ASSET_ID)
			.withStatus(Status.ACTIVE)
			.withValidTo(VALID_TO);

		// Act
		worker.expire(asset);

		// Assert
		assertThat(asset.getStatus()).isEqualTo(Status.EXPIRED);
		verify(assetRepositoryMock).save(asset);
	}
}
