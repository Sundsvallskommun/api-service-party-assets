package se.sundsvall.partyassets.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.model.StatusEntity;

class StatusMapperTest {

	private static Stream<Arguments> toEntityArgumentProvider() {
		return Stream.of(
			Arguments.of(List.of("REASON_1", "REASON_2"), List.of("REASON_1", "REASON_2")),
			Arguments.of(List.of(" REASON_1 ", " reason_2"), List.of("REASON_1", "REASON_2")),
			Arguments.of(List.of(" Reason 1 ", "Reason 1", "Reason 3 "), List.of("REASON 1", "REASON 3")));
	}

	@Test
	void toReasonsForStatuses() {
		// Arrange
		final var activeReasons = List.of("A1", "A2", "A3");
		final var blockedReasons = List.of("B1", "B2");
		final var expiredReasons = List.of("E1");
		final var entities = List.of(
			StatusEntity.create().withName(Status.ACTIVE.name()).withReasons(activeReasons),
			StatusEntity.create().withName(Status.BLOCKED.name()).withReasons(blockedReasons),
			StatusEntity.create().withName(Status.EXPIRED.name()).withReasons(expiredReasons));

		// Act
		final var reasonMap = StatusMapper.toReasons(entities);

		// Assert
		assertThat(reasonMap).isNotNull()
			.hasSize(3)
			.extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
			.containsExactlyInAnyOrder(
				tuple(Status.ACTIVE, activeReasons),
				tuple(Status.BLOCKED, blockedReasons),
				tuple(Status.EXPIRED, expiredReasons));
	}

	@ParameterizedTest
	@MethodSource("toEntityArgumentProvider")
	void toEntity(final List<String> reasons, final List<String> expectedReasons) {
		// Arrange
		final var status = Status.values()[new Random().nextInt(0, Status.values().length)];
		final var municipalityId = "1234";

		// Act
		final var entity = StatusMapper.toEntity(status, reasons, municipalityId);

		// Assert
		assertThat(entity.getName()).isEqualTo(status.name());
		assertThat(entity.getReasons()).isEqualTo(expectedReasons);
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
	}

}
