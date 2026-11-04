package se.sundsvall.partyassets.integration.events;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import se.sundsvall.partyassets.integration.events.AssetEvent.Asset;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static se.sundsvall.partyassets.TestFactory.getAssetEntity;
import static se.sundsvall.partyassets.api.model.Status.ACTIVE;
import static se.sundsvall.partyassets.integration.events.AssetEvent.EventType.UPDATED;
import static se.sundsvall.partyassets.integration.events.AssetEventMapper.toAssetEvent;
import static se.sundsvall.partyassets.integration.events.AssetEventMapper.toRepublishedAssetEvent;

class AssetEventMapperTest {

	@Test
	void toAssetEventFromEntity() {
		// Arrange
		final var occurredAt = OffsetDateTime.now();
		final var entity = getAssetEntity("id", "partyId").withVersion(7L).withOrigin("origin");

		// Act
		final var event = toAssetEvent(entity, UPDATED, occurredAt);

		// Assert
		assertThat(event).isEqualTo(new AssetEvent(UPDATED, "municipalityId", 7L, occurredAt,
			new Asset("id", "assetId", "origin", "partyId", "type", "title", "description", ACTIVE, "statusReason", LocalDate.of(2010, 1, 1), LocalDate.of(2010, 1, 1))));
	}

	@Test
	void toRepublishedAssetEventAtLastUpdate() {
		// Arrange
		final var entity = getAssetEntity("id", "partyId").withVersion(7L);

		// Act
		final var event = toRepublishedAssetEvent(entity);

		// Assert
		assertThat(event).isEqualTo(toAssetEvent(entity, UPDATED, entity.getUpdated()));
	}

	@Test
	void toRepublishedAssetEventNeverUpdated() {
		// Arrange
		final var entity = getAssetEntity("id", "partyId").withVersion(0L).withUpdated(null);

		// Act
		final var event = toRepublishedAssetEvent(entity);

		// Assert
		assertThat(event).isEqualTo(toAssetEvent(entity, UPDATED, entity.getCreated()));
	}

	@Test
	void toRepublishedAssetEventWithoutTimes() {
		// Arrange
		final var entity = getAssetEntity("id", "partyId").withVersion(0L).withCreated(null).withUpdated(null);

		// Act
		final var event = toRepublishedAssetEvent(entity);

		// Assert
		assertThat(event).usingRecursiveComparison().ignoringFields("occurredAt").isEqualTo(toAssetEvent(entity, UPDATED, null));
		assertThat(event.occurredAt()).isCloseTo(OffsetDateTime.now(), within(2, SECONDS));
	}
}
