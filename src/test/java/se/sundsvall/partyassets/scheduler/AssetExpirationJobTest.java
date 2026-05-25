package se.sundsvall.partyassets.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssetExpirationJobTest {

	@Mock
	private AssetExpirationWorker assetExpirationWorkerMock;

	@InjectMocks
	private AssetExpirationJob job;

	@Test
	void runDelegatesToWorker() {
		job.run();

		verify(assetExpirationWorkerMock).expireAssets();
	}
}
