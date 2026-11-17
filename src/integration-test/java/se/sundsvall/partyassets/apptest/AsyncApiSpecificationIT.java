package se.sundsvall.partyassets.apptest;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.dept44.util.ResourceUtils;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.integration.events.AssetEvent;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.writeString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static se.sundsvall.partyassets.api.model.Status.ACTIVE;
import static se.sundsvall.partyassets.integration.events.AssetEvent.EventType.UPDATED;

/**
 * Guards the AsyncAPI document the way OpenApiSpecificationIT guards the OpenAPI document, except that it compares the
 * text instead of the parsed YAML.
 * <p>
 * Comparing text works because the document comes out the same every time. AssetEventsAsyncApi builds channels,
 * operations and keys in a fixed order, swagger-core sorts the schema names and the required lists, properties follow
 * the record components, and there is no servers block with a random port. Text also shows changes that parsing hides,
 * such as quoting, and a failure is a line diff. The cost is that a cosmetic change, for example after a Jackson or
 * swagger-core upgrade, fails the test too. Then copy target/asyncapi.yaml over the baseline. Newlines are normalized,
 * so a Windows checkout with CRLF passes.
 */
@ActiveProfiles("it")
@SpringBootTest(
	webEnvironment = WebEnvironment.RANDOM_PORT,
	classes = Application.class,
	properties = {
		"spring.main.banner-mode=off",
		"logging.level.se.sundsvall.dept44.payload=OFF",
		"wiremock.server.port=10101"
	})
@AutoConfigureTestRestTemplate
class AsyncApiSpecificationIT {

	private static final YAMLMapper YAML_MAPPER = new YAMLMapper();
	private static final String PATH = "/api-docs/asyncapi.yaml";

	@Value("classpath:/api/asyncapi.yaml")
	private Resource asyncApiResource;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private JsonMapper jsonMapper;

	@Test
	void compareAsyncApiSpecifications() throws IOException {
		// Written before the baseline is read, so that a first run leaves a file to copy
		final var currentAsyncApiSpecification = fetchAsyncApiSpecification();
		writeString(Path.of("target/asyncapi.yaml"), currentAsyncApiSpecification);

		assertThat(currentAsyncApiSpecification).isEqualToNormalizingNewlines(ResourceUtils.asString(asyncApiResource));
	}

	@Test
	void servedSchemasMatchSerializedEvent() {
		final var document = YAML_MAPPER.readTree(fetchAsyncApiSpecification());
		// Every field is set, since the service's mapper leaves out null fields
		final JsonNode event = jsonMapper.valueToTree(new AssetEvent(UPDATED, "2281", 4L, OffsetDateTime.parse("2026-11-03T14:12:09.123+01:00"),
			new AssetEvent.Asset("id", "assetId", "origin", "partyId", "type", "title", "description", ACTIVE, "statusReason",
				LocalDate.parse("2026-01-01"), LocalDate.parse("2027-12-31"))));

		assertThat(keys(event)).containsExactlyInAnyOrderElementsOf(keys(document.at("/components/schemas/AssetEvent/properties")));
		assertThat(keys(event.get("asset"))).containsExactlyInAnyOrderElementsOf(keys(document.at("/components/schemas/Asset/properties")));
		assertThat(event.get("occurredAt").isString()).isTrue();
		assertThat(event.at("/asset/issued").asString()).isEqualTo("2026-01-01");
	}

	@Test
	void versionIsAQuotedString() {
		final var version = YAML_MAPPER.readTree(fetchAsyncApiSpecification()).at("/info/version");

		// Unquoted, a version such as 1.10 reads back as the number 1.1 for every consumer
		assertThat(version.isString()).isTrue();
		assertThat(version.asString()).isEqualTo(AssetEvent.CONTRACT_VERSION);
	}

	/**
	 * Reads the body as bytes. The YAML message converter would try to parse application/yaml into the String.
	 */
	private String fetchAsyncApiSpecification() {
		return restTemplate.execute(PATH, GET, null, response -> new String(response.getBody().readAllBytes(), UTF_8));
	}

	private static List<String> keys(final JsonNode node) {
		return node.properties().stream().map(Map.Entry::getKey).toList();
	}
}
