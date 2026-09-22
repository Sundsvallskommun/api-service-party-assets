package se.sundsvall.partyassets.scheduler;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetExpirationJobTest {

	@Mock
	private AssetExpirationWorker assetExpirationWorkerMock;

	@Mock
	private Dept44HealthUtility dept44HealthUtilityMock;

	@InjectMocks
	private AssetExpirationJob job;

	@Test
	void runWithNothingToExpire() {
		when(assetExpirationWorkerMock.findExpirableAssetIds()).thenReturn(List.of());

		job.run();

		verify(assetExpirationWorkerMock).findExpirableAssetIds();
		verifyNoMoreInteractions(assetExpirationWorkerMock);
	}

	@Test
	void runExpiresEachAssetSeparately() {
		when(assetExpirationWorkerMock.findExpirableAssetIds()).thenReturn(List.of("asset-1", "asset-2"));

		job.run();

		verify(assetExpirationWorkerMock).findExpirableAssetIds();
		verify(assetExpirationWorkerMock).expire("asset-1");
		verify(assetExpirationWorkerMock).expire("asset-2");
		verifyNoMoreInteractions(assetExpirationWorkerMock);
	}

	@Test
	void runContinuesAfterAFailure() {
		when(assetExpirationWorkerMock.findExpirableAssetIds()).thenReturn(List.of("asset-1", "asset-2"));
		doThrow(new IllegalStateException("nope")).when(assetExpirationWorkerMock).expire("asset-1");

		job.run();

		verify(assetExpirationWorkerMock).expire("asset-1");
		verify(assetExpirationWorkerMock).expire("asset-2");
	}

	@Test
	void runReportsUnhealthyWhenAnAssetCouldNotBeExpired() {
		when(assetExpirationWorkerMock.findExpirableAssetIds()).thenReturn(List.of("asset-1", "asset-2"));
		doThrow(new IllegalStateException("nope")).when(assetExpirationWorkerMock).expire("asset-1");

		job.run();

		verify(dept44HealthUtilityMock).setHealthIndicatorUnhealthy("asset-expiration", "1 asset(s) could not be expired");
		verifyNoMoreInteractions(dept44HealthUtilityMock);
	}

	@Test
	void runLeavesTheHealthAloneWhenEveryAssetExpired() {
		when(assetExpirationWorkerMock.findExpirableAssetIds()).thenReturn(List.of("asset-1", "asset-2"));

		job.run();

		verifyNoInteractions(dept44HealthUtilityMock);
	}

	@Test
	void runDoesNotSwallowAFailureFromTheLookup() {
		when(assetExpirationWorkerMock.findExpirableAssetIds()).thenThrow(new IllegalStateException("nope"));

		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> job.run());

		verify(assetExpirationWorkerMock).findExpirableAssetIds();
		verify(assetExpirationWorkerMock, never()).expire(anyString());
		verifyNoMoreInteractions(assetExpirationWorkerMock);
	}
}
