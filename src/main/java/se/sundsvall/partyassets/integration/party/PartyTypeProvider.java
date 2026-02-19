package se.sundsvall.partyassets.integration.party;

import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static generated.se.sundsvall.party.PartyType.PRIVATE;
import static java.lang.String.format;
import static org.zalando.problem.Status.NOT_FOUND;

@Component
public class PartyTypeProvider {

	private final PartyClient partyClient;

	public PartyTypeProvider(final PartyClient partyClient) {
		this.partyClient = partyClient;
	}

	public se.sundsvall.partyassets.integration.db.model.PartyType calculatePartyType(final String municipalityId, final String partyId) {
		if (partyClient.getLegalId(municipalityId, PRIVATE, partyId).isPresent()) {
			return se.sundsvall.partyassets.integration.db.model.PartyType.PRIVATE;
		}
		if (partyClient.getLegalId(municipalityId, ENTERPRISE, partyId).isPresent()) {
			return se.sundsvall.partyassets.integration.db.model.PartyType.ENTERPRISE;
		}

		throw Problem.valueOf(NOT_FOUND, format("PartyId '%s' could not be found as a private customer or an enterprise customer", partyId));
	}

}
