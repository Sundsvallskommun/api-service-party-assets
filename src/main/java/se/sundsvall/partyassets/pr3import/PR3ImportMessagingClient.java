package se.sundsvall.partyassets.pr3import;

import static se.sundsvall.partyassets.pr3import.PR3ImportConfiguration.INTEGRATION_NAME;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import generated.se.sundsvall.messaging.EmailRequest;

@FeignClient(
    name = INTEGRATION_NAME,
    url = "${pr3import.messaging-integration.url}",
    configuration = PR3ImportConfiguration.class
)
@ConditionalOnProperty(name = "pr3import.enabled", havingValue = "true", matchIfMissing = true)
interface PR3ImportMessagingClient {

    @PostMapping("/email")
    void sendEmail(@RequestBody EmailRequest request);
}
