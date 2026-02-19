package se.sundsvall.partyassets.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.StatusRepository;
import se.sundsvall.partyassets.integration.db.model.StatusEntity;
import se.sundsvall.partyassets.integration.db.model.StatusEntityId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private StatusRepository repositoryMock;

	@Captor
	private ArgumentCaptor<StatusEntity> entityCaptor;

	@InjectMocks
	private StatusService service;

	@Test
	void getReasonsForAllStatuses() {
		// Arrange
		final var status = Status.EXPIRED;
		final var reasons = List.of("REASON_1", "REASON_2");

		when(repositoryMock.findAllByMunicipalityId(MUNICIPALITY_ID)).thenReturn(List.of(StatusEntity.create().withMunicipalityId(MUNICIPALITY_ID).withName(status.name()).withReasons(reasons)));

		// Act
		final var reasonsForAllStatuses = service.getReasonsForAllStatuses(MUNICIPALITY_ID);

		// Assert
		verify(repositoryMock).findAllByMunicipalityId(MUNICIPALITY_ID);
		verifyNoMoreInteractions(repositoryMock);
		assertThat(reasonsForAllStatuses).isNotEmpty()
			.extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
			.containsExactly(tuple(status, reasons));
	}

	@Test
	void getExistingReasons() {
		// Arrange
		final var status = Status.BLOCKED;
		final var reasons = List.of("BLOCKED_REASON_1", "BLOCKED_REASON_2");

		when(repositoryMock.findByNameAndMunicipalityId(status.name(), MUNICIPALITY_ID)).thenReturn(Optional.of(StatusEntity.create().withReasons(reasons)));

		// Act
		final var blockedReasons = service.getReasons(MUNICIPALITY_ID, status);

		// Assert
		verify(repositoryMock).findByNameAndMunicipalityId(status.name(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(repositoryMock);
		assertThat(blockedReasons).isEqualTo(reasons);
	}

	@Test
	void getNonExistingReasons() {
		// Arrange
		final var status = Status.BLOCKED;
		when(repositoryMock.findByNameAndMunicipalityId(status.name(), MUNICIPALITY_ID)).thenReturn(Optional.empty());

		// Act
		final var blockedReasons = service.getReasons(MUNICIPALITY_ID, status);

		// Assert
		verify(repositoryMock).findByNameAndMunicipalityId(Status.BLOCKED.name(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(repositoryMock);
		assertThat(blockedReasons).isEmpty();
	}

	@Test
	void createReasons() {
		// Arrange
		final var status = Status.BLOCKED;
		final var reasons = List.of("BLOCKED_REASON_1", "BLOCKED_REASON_2");

		// Act
		service.createReasons(MUNICIPALITY_ID, status, reasons);

		// Assert
		verify(repositoryMock).existsByNameAndMunicipalityId(status.name(), MUNICIPALITY_ID);
		verify(repositoryMock).save(entityCaptor.capture());
		assertThat(entityCaptor.getValue().getName()).isEqualTo(status.name());
		assertThat(entityCaptor.getValue().getReasons()).isEqualTo(reasons);
		assertThat(entityCaptor.getValue().getCreated()).isNull();
		assertThat(entityCaptor.getValue().getUpdated()).isNull();
	}

	@Test
	void createExistingReasons() {
		// Arrange
		final var status = Status.BLOCKED;
		final var reasons = List.of("BLOCKED_REASON_1", "BLOCKED_REASON_2");

		when(repositoryMock.existsByNameAndMunicipalityId(status.name(), MUNICIPALITY_ID)).thenReturn(true);

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.createReasons(MUNICIPALITY_ID, status, reasons));

		// Assert
		verify(repositoryMock).existsByNameAndMunicipalityId(status.name(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(repositoryMock);
		assertThat(e.getStatus()).isEqualTo(org.zalando.problem.Status.CONFLICT);
		assertThat(e.getMessage()).isEqualTo("Conflict: Statusreasons already exists for status BLOCKED");
	}

	@Test
	void deleteReasons() {
		// Arrange
		final var status = Status.ACTIVE;

		when(repositoryMock.existsByNameAndMunicipalityId(status.name(), MUNICIPALITY_ID)).thenReturn(true);

		// Act
		service.deleteReasons(MUNICIPALITY_ID, status);

		// Assert
		verify(repositoryMock).existsByNameAndMunicipalityId(status.name(), MUNICIPALITY_ID);
		verify(repositoryMock).deleteById(new StatusEntityId(status.name(), MUNICIPALITY_ID));
	}

	@Test
	void deleteNonExistingReasons() {
		// Arrange
		final var status = Status.ACTIVE;

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.deleteReasons(MUNICIPALITY_ID, status));

		// Assert
		verify(repositoryMock).existsByNameAndMunicipalityId(status.name(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(repositoryMock);
		assertThat(e.getStatus()).isEqualTo(org.zalando.problem.Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Status ACTIVE does not have any statusreasons to delete");
	}

}
