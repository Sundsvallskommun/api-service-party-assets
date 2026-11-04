package se.sundsvall.partyassets.integration.events;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import se.sundsvall.dept44.rabbitmq.Dept44RabbitPublisher;
import se.sundsvall.dept44.rabbitmq.OutgoingMessage;
import se.sundsvall.partyassets.integration.events.AssetEvent.EventType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static se.sundsvall.partyassets.TestFactory.getAssetEntity;
import static se.sundsvall.partyassets.integration.events.AssetEvent.EventType.UPDATED;
import static se.sundsvall.partyassets.integration.events.AssetEventMapper.toAssetEvent;

@ExtendWith(MockitoExtension.class)
class AssetEventPublisherTest {

	private static final String EXCHANGE = "api-fabriken.party-assets";

	@Mock
	private Dept44RabbitPublisher publisherMock;

	@Captor
	private ArgumentCaptor<OutgoingMessage> messageCaptor;

	@ParameterizedTest
	@EnumSource(EventType.class)
	void onCommit(final EventType eventType) {
		// Arrange
		final var event = toAssetEvent(getAssetEntity("id", "partyId").withVersion(3L), eventType, OffsetDateTime.now());

		// Act
		new AssetEventPublisher(publisherMock, EXCHANGE).onCommit(event);

		// Assert
		verify(publisherMock).publish(messageCaptor.capture());
		assertThat(messageCaptor.getValue()).satisfies(message -> {
			assertThat(message.getExchange()).isEqualTo(EXCHANGE);
			assertThat(message.getRoutingKey()).isEqualTo(eventType.getRoutingKey());
			assertThat(message.getPayload()).isSameAs(event);
			// The publisher generates it
			assertThat(message.getMessageId()).isNull();
		});
	}

	@Test
	void onCommitWhenPublishFails() {
		// Arrange
		final var event = toAssetEvent(getAssetEntity("id", "partyId").withVersion(3L), UPDATED, OffsetDateTime.now());
		doThrow(new AmqpConnectException(new RuntimeException("Connection refused"))).when(publisherMock).publish(any());
		final var publisher = new AssetEventPublisher(publisherMock, EXCHANGE);

		// Act and assert
		assertThatNoException().isThrownBy(() -> publisher.onCommit(event));
		verify(publisherMock).publish(any());
	}

	@Test
	void publishWhenPublishFails() {
		// Arrange
		final var event = toAssetEvent(getAssetEntity("id", "partyId").withVersion(3L), UPDATED, OffsetDateTime.now());
		doThrow(new AmqpConnectException(new RuntimeException("Connection refused"))).when(publisherMock).publish(any());
		final var publisher = new AssetEventPublisher(publisherMock, EXCHANGE);

		// Act and assert
		assertThatExceptionOfType(AmqpException.class).isThrownBy(() -> publisher.publish(event));
	}
}
