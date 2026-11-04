package se.sundsvall.partyassets.integration.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import se.sundsvall.dept44.rabbitmq.Dept44RabbitPublisher;
import se.sundsvall.dept44.rabbitmq.OutgoingMessage;

/**
 * Publishes the asset events, and exists only while RabbitMQ is on.
 */
@Component
@ConditionalOnBooleanProperty("dept44.rabbitmq.enabled")
public class AssetEventPublisher {

	private static final Logger LOG = LoggerFactory.getLogger(AssetEventPublisher.class);

	private final Dept44RabbitPublisher publisher;
	private final String exchange;

	AssetEventPublisher(final Dept44RabbitPublisher publisher, @Value("${integration.asset-events.exchange}") final String exchange) {
		this.publisher = publisher;
		this.exchange = exchange;
	}

	/**
	 * Publishes an event and returns once the broker has it.
	 *
	 * @throws AmqpException if the broker does not take it
	 */
	public void publish(final AssetEvent event) {
		publisher.publish(OutgoingMessage.to(exchange, event.eventType().getRoutingKey()).withPayload(event));
	}

	/**
	 * Publishes the event of a change once the change is committed, so a change that is rolled back is never published.
	 * A publish that fails is logged and the event is lost: the change is committed and the caller has its answer by
	 * then. Publishing the asset again recovers it.
	 */
	@TransactionalEventListener
	void onCommit(final AssetEvent event) {
		try {
			publish(event);
		} catch (final AmqpException e) {
			LOG.error("The {} event of asset {} at version {} was not published", event.eventType(), event.asset().id(), event.version(), e);
		}
	}
}
