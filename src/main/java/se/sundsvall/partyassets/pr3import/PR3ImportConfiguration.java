package se.sundsvall.partyassets.pr3import;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(name = "pr3import.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PR3ImportProperties.class)
class PR3ImportConfiguration implements WebMvcConfigurer {
}
