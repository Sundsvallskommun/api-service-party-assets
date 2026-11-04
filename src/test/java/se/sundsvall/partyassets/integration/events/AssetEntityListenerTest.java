package se.sundsvall.partyassets.integration.events;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.sundsvall.partyassets.TestFactory.getAssetEntity;
import static se.sundsvall.partyassets.integration.events.AssetEvent.EventType.CREATED;
import static se.sundsvall.partyassets.integration.events.AssetEvent.EventType.DELETED;
import static se.sundsvall.partyassets.integration.events.AssetEvent.EventType.UPDATED;
import static se.sundsvall.partyassets.integration.events.AssetEventMapper.toAssetEvent;

@ExtendWith(MockitoExtension.class)
class AssetEntityListenerTest {

	@Mock
	private ApplicationEventPublisher eventPublisherMock;

	@Captor
	private ArgumentCaptor<AssetEvent> eventCaptor;

	@InjectMocks
	private AssetEntityListener listener;

	@Test
	void created() {
		// Arrange
		final var entity = getAssetEntity("id", "partyId").withVersion(0L);

		// Act
		listener.created(entity);

		// Assert
		verify(eventPublisherMock).publishEvent(toAssetEvent(entity, CREATED, entity.getCreated()));
		verifyNoMoreInteractions(eventPublisherMock);
	}

	@Test
	void updated() {
		// Arrange
		final var entity = getAssetEntity("id", "partyId").withVersion(3L);

		// Act
		listener.updated(entity);

		// Assert
		verify(eventPublisherMock).publishEvent(toAssetEvent(entity, UPDATED, entity.getUpdated()));
		verifyNoMoreInteractions(eventPublisherMock);
	}

	@Test
	void deleted() {
		// Arrange
		final var entity = getAssetEntity("id", "partyId").withVersion(3L);

		// Act
		listener.deleted(entity);

		// Assert
		verify(eventPublisherMock).publishEvent(eventCaptor.capture());
		assertThat(eventCaptor.getValue()).usingRecursiveComparison().ignoringFields("occurredAt").isEqualTo(toAssetEvent(entity, DELETED, null));
		assertThat(eventCaptor.getValue().occurredAt()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
		verifyNoMoreInteractions(eventPublisherMock);
	}
}
