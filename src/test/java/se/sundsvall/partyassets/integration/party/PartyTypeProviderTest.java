package se.sundsvall.partyassets.integration.party;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.partyassets.integration.db.model.PartyType;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static generated.se.sundsvall.party.PartyType.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartyTypeProviderTest {

	@Mock
	private PartyClient partyClientMock;

	@InjectMocks
	private PartyTypeProvider partyTypeProvider;

	@Test
	void testPrivateParty() {
		final var uuid = UUID.randomUUID().toString();
		final var municipalityId = "2281";

		when(partyClientMock.getLegalId(municipalityId, PRIVATE, uuid)).thenReturn(Optional.of("190101011234"));

		assertThat(partyTypeProvider.calculatePartyType(municipalityId, uuid)).isEqualTo(PartyType.PRIVATE);
		verify(partyClientMock).getLegalId(municipalityId, PRIVATE, uuid);
		verify(partyClientMock, never()).getLegalId(municipalityId, ENTERPRISE, uuid);
	}

	@Test
	void testEnterpriseParty() {
		final var uuid = UUID.randomUUID().toString();
		final var municipalityId = "2281";

		when(partyClientMock.getLegalId(municipalityId, PRIVATE, uuid)).thenReturn(Optional.empty());
		when(partyClientMock.getLegalId(municipalityId, ENTERPRISE, uuid)).thenReturn(Optional.of("5566112233"));

		assertThat(partyTypeProvider.calculatePartyType(municipalityId, uuid)).isEqualTo(PartyType.ENTERPRISE);
		verify(partyClientMock).getLegalId(municipalityId, PRIVATE, uuid);
		verify(partyClientMock).getLegalId(municipalityId, ENTERPRISE, uuid);

	}

	@Test
	void testNoPartyFound() {
		final var uuid = UUID.randomUUID().toString();
		final var municipalityId = "2281";

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> partyTypeProvider.calculatePartyType(municipalityId, uuid))
			.withMessage("Not Found: PartyId '" + uuid + "' could not be found as a private customer or an enterprise customer");
		verify(partyClientMock).getLegalId(municipalityId, PRIVATE, uuid);
		verify(partyClientMock).getLegalId(municipalityId, ENTERPRISE, uuid);
	}

}
