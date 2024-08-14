package se.sundsvall.partyassets.integration.party;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static se.sundsvall.partyassets.integration.party.configuration.PartyConfiguration.CLIENT_ID;

import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import se.sundsvall.partyassets.integration.party.configuration.PartyConfiguration;

import generated.se.sundsvall.party.PartyType;

@FeignClient(name = CLIENT_ID, url = "${integration.party.url}", configuration = PartyConfiguration.class, dismiss404 = true)
public interface PartyClient {

	/**
	 * Get legal-ID by type and partyId.
	 *
	 * @param municipalityId the municipality ID.
	 * @param partyType the type of party.
	 * @param partyId the ID of the party. I.e. the personId or organizationId.
	 * @return an optional string containing the legalId that corresponds to the provided partyType and partyId.
	 * @throws org.zalando.problem.ThrowableProblem
	 */
	@GetMapping(path = "/{municipalityId}/{type}/{partyId}/legalId", produces = {TEXT_PLAIN_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	Optional<String> getLegalId(@PathVariable("municipalityId") String municipalityId, @PathVariable("type") PartyType partyType, @PathVariable("partyId") String partyId);

	/**
	 * Get partyId by type and legal-ID.
	 *
	 * @param municipalityId the municipality ID.
	 * @param partyType the type of party.
	 * @param legalId the legal-ID.
	 * @return an optional string containing the partyId that corresponds to the provided partyType and legalId.
	 * @throws org.zalando.problem.ThrowableProblem
	 */
	@GetMapping(path = "/{municipalityId}/{type}/{legalId}/partyId", produces = {TEXT_PLAIN_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	Optional<String> getPartyId(@PathVariable("municipalityId") String municipalityId, @PathVariable("type") PartyType partyType, @PathVariable("legalId") String legalId);

}
