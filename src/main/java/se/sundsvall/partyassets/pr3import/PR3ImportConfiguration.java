package se.sundsvall.partyassets.pr3import;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Configuration
@Import(FeignConfiguration.class)
@ConditionalOnProperty(name = "pr3import.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PR3ImportProperties.class)
class PR3ImportConfiguration implements WebMvcConfigurer {

	static final String INTEGRATION_NAME = "pr3import-messaging";

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final PR3ImportProperties properties) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(INTEGRATION_NAME))
			.withRetryableOAuth2InterceptorForClientRegistration(ClientRegistration
				.withRegistrationId(INTEGRATION_NAME)
				.tokenUri(properties.messagingIntegration().oauth2().tokenUri())
				.clientId(properties.messagingIntegration().oauth2().clientId())
				.clientSecret(properties.messagingIntegration().oauth2().clientSecret())
				.authorizationGrantType(
					new AuthorizationGrantType(properties.messagingIntegration().oauth2().grantType()))
				.build())
			.withRequestTimeoutsInSeconds(properties.messagingIntegration().connectTimeout(),
				properties.messagingIntegration().readTimeout())
			.composeCustomizersToOne();
	}
}
