package se.sundsvall.partyassets.pr3import;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "pr3import")
record PR3ImportProperties(StaticAssetInfo staticAssetInfo, MessagingIntegration messagingIntegration, Map<String, Sender> senders) {

	record StaticAssetInfo(
		@DefaultValue("PR3") String origin,

		@DefaultValue("PARKING_PERMIT") String type,

		@DefaultValue("Parkeringstillstånd för rörelsehindrade") String description,

		@DefaultValue("2281") String municipalityId) {}

	record MessagingIntegration(String url, @DefaultValue("5") int connectTimeout, @DefaultValue("30") int readTimeout) {}

	record Sender(
		@DefaultValue("name") String name,

		@DefaultValue("email") String email) {}
}
