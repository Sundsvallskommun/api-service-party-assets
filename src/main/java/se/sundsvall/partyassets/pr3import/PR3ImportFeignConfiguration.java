package se.sundsvall.partyassets.pr3import;

import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
class PR3ImportFeignConfiguration {

	static final String INTEGRATION_NAME = "pr3import-messaging";

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(ClientRegistrationRepository clientRepository, final PR3ImportProperties properties) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(INTEGRATION_NAME))
			.withRetryableOAuth2InterceptorForClientRegistration(clientRepository.findByRegistrationId(INTEGRATION_NAME))
			.withRequestTimeoutsInSeconds(properties.messagingIntegration().connectTimeout(),
				properties.messagingIntegration().readTimeout())
			.composeCustomizersToOne();
	}
}
