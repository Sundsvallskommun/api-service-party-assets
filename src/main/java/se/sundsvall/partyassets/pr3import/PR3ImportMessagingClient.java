package se.sundsvall.partyassets.pr3import;

import generated.se.sundsvall.messaging.EmailRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static se.sundsvall.partyassets.pr3import.PR3ImportFeignConfiguration.INTEGRATION_NAME;

@FeignClient(
	name = INTEGRATION_NAME,
	url = "${pr3import.messaging-integration.url}",
	configuration = PR3ImportFeignConfiguration.class)
@ConditionalOnProperty(name = "pr3import.enabled", havingValue = "true", matchIfMissing = true)
@CircuitBreaker(name = INTEGRATION_NAME)
interface PR3ImportMessagingClient {

	@PostMapping("/{municipalityId}/email")
	void sendEmail(@PathVariable String municipalityId, @RequestBody EmailRequest request);
}
