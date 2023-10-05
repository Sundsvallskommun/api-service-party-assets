package se.sundsvall.partyassets.integration.party;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static generated.se.sundsvall.party.PartyType.PRIVATE;
import static java.lang.String.format;
import static org.zalando.problem.Status.NOT_FOUND;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;

@Component
public class PartyTypeProvider {

	@Autowired
	private PartyClient partyClient;

	public se.sundsvall.partyassets.integration.db.model.PartyType calculatePartyType(String partyId) {
		if (partyClient.getLegalId(PRIVATE, partyId).isPresent()) {
			return se.sundsvall.partyassets.integration.db.model.PartyType.PRIVATE;
		}
		if (partyClient.getLegalId(ENTERPRISE, partyId).isPresent()) {
			return se.sundsvall.partyassets.integration.db.model.PartyType.ENTERPRISE;
		}
		
		throw Problem.valueOf(NOT_FOUND, format("PartyId '%s' could not be found as a private customer or an enterprise customer", partyId));
	}
}
