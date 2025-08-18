package se.sundsvall.partyassets.integration.party;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static se.sundsvall.partyassets.integration.party.configuration.PartyConfiguration.CLIENT_ID;

import generated.se.sundsvall.party.PartyType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.partyassets.integration.party.configuration.PartyConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.party.url}", configuration = PartyConfiguration.class, dismiss404 = true)
@CircuitBreaker(name = CLIENT_ID)
public interface PartyClient {

	/**
	 * Get legal-ID by type and partyId.
	 *
	 * @param  municipalityId                       the municipality ID.
	 * @param  type                                 the type of party.
	 * @param  partyId                              the ID of the party. I.e. the personId or organizationId.
	 * @return                                      an optional string containing the legalId that corresponds to the
	 *                                              provided partyType and partyId.
	 * @throws org.zalando.problem.ThrowableProblem
	 */
	@GetMapping(path = "/{municipalityId}/{type}/{partyId}/legalId", produces = TEXT_PLAIN_VALUE)
	Optional<String> getLegalId(@PathVariable String municipalityId, @PathVariable PartyType type, @PathVariable String partyId);

	/**
	 * Get partyId by type and legal-ID.
	 *
	 * @param  municipalityId                       the municipality ID.
	 * @param  type                                 the type of party.
	 * @param  legalId                              the legal-ID.
	 * @return                                      an optional string containing the partyId that corresponds to the
	 *                                              provided partyType and legalId.
	 * @throws org.zalando.problem.ThrowableProblem
	 */
	@GetMapping(path = "/{municipalityId}/{type}/{legalId}/partyId", produces = TEXT_PLAIN_VALUE)
	Optional<String> getPartyId(@PathVariable String municipalityId, @PathVariable PartyType type, @PathVariable String legalId);
}
