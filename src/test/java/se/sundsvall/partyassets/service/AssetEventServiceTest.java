package se.sundsvall.partyassets.service;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.data.domain.Limit;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.events.AssetEventPublisher;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.partyassets.TestFactory.getAssetEntity;
import static se.sundsvall.partyassets.integration.events.AssetEventMapper.toRepublishedAssetEvent;

@ExtendWith(MockitoExtension.class)
class AssetEventServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final Limit PAGE_SIZE = Limit.of(100);

	@Mock
	private AssetRepository repositoryMock;

	@Mock
	private AssetEventPublisher publisherMock;

	@Test
	void republish() {
		// Arrange
		final var first = getAssetEntity("id-1", "partyId").withVersion(1L);
		final var second = getAssetEntity("id-2", "partyId").withVersion(2L);
		final var third = getAssetEntity("id-3", "partyId").withVersion(3L);
		when(repositoryMock.findByMunicipalityIdAndIdGreaterThanOrderById(MUNICIPALITY_ID, "", PAGE_SIZE)).thenReturn(List.of(first, second));
		when(repositoryMock.findByMunicipalityIdAndIdGreaterThanOrderById(MUNICIPALITY_ID, "id-2", PAGE_SIZE)).thenReturn(List.of(third));
		when(repositoryMock.findByMunicipalityIdAndIdGreaterThanOrderById(MUNICIPALITY_ID, "id-3", PAGE_SIZE)).thenReturn(List.of());

		// Act
		new AssetEventService(repositoryMock, Optional.of(publisherMock)).republish(MUNICIPALITY_ID);

		// Assert
		final var inOrder = inOrder(publisherMock);
		inOrder.verify(publisherMock).publish(toRepublishedAssetEvent(first));
		inOrder.verify(publisherMock).publish(toRepublishedAssetEvent(second));
		inOrder.verify(publisherMock).publish(toRepublishedAssetEvent(third));
		verifyNoMoreInteractions(publisherMock);
	}

	@Test
	void republishWithoutAssets() {
		// Arrange
		when(repositoryMock.findByMunicipalityIdAndIdGreaterThanOrderById(MUNICIPALITY_ID, "", PAGE_SIZE)).thenReturn(List.of());

		// Act
		new AssetEventService(repositoryMock, Optional.of(publisherMock)).republish(MUNICIPALITY_ID);

		// Assert
		verifyNoInteractions(publisherMock);
	}

	@Test
	void republishWhenPublishFails() {
		// Arrange
		final var service = new AssetEventService(repositoryMock, Optional.of(publisherMock));
		when(repositoryMock.findByMunicipalityIdAndIdGreaterThanOrderById(MUNICIPALITY_ID, "", PAGE_SIZE)).thenReturn(List.of(getAssetEntity("id-1", "partyId")));
		doThrow(new AmqpConnectException(new RuntimeException("Connection refused"))).when(publisherMock).publish(any());

		// Act and assert
		assertThatExceptionOfType(AmqpException.class).isThrownBy(() -> service.republish(MUNICIPALITY_ID));
		verify(repositoryMock).findByMunicipalityIdAndIdGreaterThanOrderById(MUNICIPALITY_ID, "", PAGE_SIZE);
		verifyNoMoreInteractions(repositoryMock);
	}

	@Test
	void republishWhenRabbitMqIsOff() {
		// Arrange
		final var service = new AssetEventService(repositoryMock, Optional.empty());

		// Act and assert
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> service.republish(MUNICIPALITY_ID))
			.withMessage("Service Unavailable: RabbitMQ is off, so no asset events can be published");
		verifyNoInteractions(repositoryMock);
	}
}
