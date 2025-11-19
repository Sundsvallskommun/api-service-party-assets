package se.sundsvall.partyassets.pr3import;

import jakarta.validation.constraints.NotBlank;
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

	record MessagingIntegration(String url, Oauth2 oauth2, @DefaultValue("5") int connectTimeout, @DefaultValue("30") int readTimeout) {

		record Oauth2(
			@NotBlank String tokenUri,

			@NotBlank String clientId,

			@NotBlank String clientSecret,

			@DefaultValue("client_credentials") String grantType) {}
	}

	record Sender(
		@DefaultValue("name") String name,

		@DefaultValue("email") String email) {}
}
