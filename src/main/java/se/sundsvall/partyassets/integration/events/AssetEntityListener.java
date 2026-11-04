package se.sundsvall.partyassets.integration.events;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.context.ApplicationEventPublisher;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;
import static se.sundsvall.partyassets.integration.events.AssetEvent.EventType.CREATED;
import static se.sundsvall.partyassets.integration.events.AssetEvent.EventType.DELETED;
import static se.sundsvall.partyassets.integration.events.AssetEvent.EventType.UPDATED;
import static se.sundsvall.partyassets.integration.events.AssetEventMapper.toAssetEvent;

/**
 * Describes every change of an asset as an {@link AssetEvent}, which {@link AssetEventPublisher} publishes once the
 * change is committed. Every write of an asset goes through the entity, so this sees them all. A bulk update or delete
 * by a query would not be seen, and must not be added.
 * <p>
 * The event is made here, at the flush, and not at commit: the version has grown by then, and a second change of the
 * same asset in the same transaction must not overwrite the first. Spring creates the listener, through the bean
 * container Spring sets on Hibernate.
 */
public class AssetEntityListener {

	private final ApplicationEventPublisher eventPublisher;

	public AssetEntityListener(final ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	@PostPersist
	void created(final AssetEntity entity) {
		eventPublisher.publishEvent(toAssetEvent(entity, CREATED, entity.getCreated()));
	}

	@PostUpdate
	void updated(final AssetEntity entity) {
		eventPublisher.publishEvent(toAssetEvent(entity, UPDATED, entity.getUpdated()));
	}

	@PostRemove
	void deleted(final AssetEntity entity) {
		eventPublisher.publishEvent(toAssetEvent(entity, DELETED, now(systemDefault()).truncatedTo(MILLIS)));
	}
}
