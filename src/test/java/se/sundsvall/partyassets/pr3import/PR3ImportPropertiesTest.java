package se.sundsvall.partyassets.pr3import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.partyassets.Application;

@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
@ActiveProfiles("junit")
class PR3ImportPropertiesTest {
	@Autowired
	private PR3ImportProperties pr3ImportProperties;

	@Test
	void testPropertiesLoaded() {
		assertThat(pr3ImportProperties.staticAssetInfo().origin()).isEqualTo("someOrigin");
		assertThat(pr3ImportProperties.staticAssetInfo().type()).isEqualTo("someType");
		assertThat(pr3ImportProperties.staticAssetInfo().description()).isEqualTo("someDescription");
		assertThat(pr3ImportProperties.staticAssetInfo().municipalityId()).isEqualTo("someMunicipalityId");
		assertThat(pr3ImportProperties.messagingIntegration().oauth2().clientId()).isEqualTo("client-id");
		assertThat(pr3ImportProperties.messagingIntegration().oauth2().clientSecret()).isEqualTo("client-secret");
		assertThat(pr3ImportProperties.messagingIntegration().oauth2().tokenUri()).isEqualTo("api-gateway-url");
		assertThat(pr3ImportProperties.messagingIntegration().oauth2().grantType()).isEqualTo("client_credentials");
		assertThat(pr3ImportProperties.messagingIntegration().url()).isEqualTo("api-messaging-url");
		assertThat(pr3ImportProperties.senders().get("2260").name()).isEqualTo("someName");
		assertThat(pr3ImportProperties.senders().get("2260").email()).isEqualTo("someEmail");
	}
}
