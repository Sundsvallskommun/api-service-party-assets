package se.sundsvall.partyassets.integration.events;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json31;
import java.util.Optional;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.partyassets.integration.events.AssetEvent.EventType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.yaml.YAMLMapper;

import static java.util.Locale.ROOT;
import static org.springframework.util.StringUtils.capitalize;
import static tools.jackson.dataformat.yaml.YAMLWriteFeature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS;
import static tools.jackson.dataformat.yaml.YAMLWriteFeature.MINIMIZE_QUOTES;
import static tools.jackson.dataformat.yaml.YAMLWriteFeature.WRITE_DOC_START_MARKER;

/**
 * The AsyncAPI document of the asset events, the contract with their consumers. Each event type gets a channel and a
 * send operation, and the payload schemas are read from {@link AssetEvent} with swagger-core's converters. Its version
 * is {@link AssetEvent#CONTRACT_VERSION}, not the service's version.
 * <p>
 * The converters are a private ModelConverters, not the shared instance that springdoc adds its own converters to at
 * startup. With the shared one, the schemas would depend on whether this bean is created before or after springdoc's.
 * <p>
 * The schemas are written in OpenAPI 3.1 form, without a schemaFormat. AsyncAPI 3.0 does not list OpenAPI 3.1 as a
 * schema format, and its default format, a superset of JSON Schema draft-07, fits these flat schemas.
 */
@Component
public class AssetEventsAsyncApi {

	private static final String ASYNCAPI_VERSION = "3.0.0";
	private static final String AMQP_BINDING_VERSION = "0.3.0";
	private static final String CONTENT_TYPE = "application/json";
	private static final String MESSAGE = "AssetEvent";

	private static final JsonMapper JSON_MAPPER = new JsonMapper();
	// Quotes strings that would read back as numbers, such as a version 1.10
	private static final YAMLMapper YAML_MAPPER = YAMLMapper.builder()
		.enable(MINIMIZE_QUOTES, ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
		.disable(WRITE_DOC_START_MARKER)
		.build();

	private final String yaml;

	public AssetEventsAsyncApi(@Value("${openapi.title}") final String title, @Value("${integration.asset-events.exchange}") final String exchange) {

		final var document = YAML_MAPPER.createObjectNode().put("asyncapi", ASYNCAPI_VERSION);
		document.putObject("info").put("title", title).put("version", AssetEvent.CONTRACT_VERSION);
		document.put("defaultContentType", CONTENT_TYPE);

		final var channels = document.putObject("channels");
		final var operations = document.putObject("operations");
		for (final var eventType : EventType.values()) {
			final var channelId = "asset" + capitalize(eventType.name().toLowerCase(ROOT));
			addChannel(channels.putObject(channelId), eventType.getRoutingKey(), exchange);
			addOperation(operations.putObject("send" + capitalize(channelId)), channelId);
		}

		final var components = document.putObject("components");
		components.putObject("messages").putObject(MESSAGE)
			.put("name", MESSAGE)
			.put("contentType", CONTENT_TYPE)
			.putObject("payload").put("$ref", "#/components/schemas/" + MESSAGE);
		components.set("schemas", schemas());

		yaml = YAML_MAPPER.writeValueAsString(document);
	}

	public String yaml() {
		return yaml;
	}

	/**
	 * swagger-core is built on Jackson 2, so it writes the schemas as JSON text and Jackson 3 reads them back. That keeps
	 * Jackson 2 out of this class. Json31.pretty returns null instead of throwing when it cannot write them.
	 */
	private static JsonNode schemas() {
		final var schemas = new TreeMap<>(new ModelConverters(true).readAll(new AnnotatedType(AssetEvent.class)));
		final var json = Optional.ofNullable(Json31.pretty(schemas))
			.orElseThrow(() -> new IllegalStateException("The schemas of the asset events could not be written"));
		return JSON_MAPPER.readTree(json);
	}

	private static void addChannel(final ObjectNode channel, final String routingKey, final String exchange) {
		channel.put("address", routingKey);
		channel.putObject("messages").putObject(MESSAGE).put("$ref", "#/components/messages/" + MESSAGE);
		final var amqp = channel.putObject("bindings").putObject("amqp").put("is", "routingKey");
		amqp.putObject("exchange").put("name", exchange).put("type", "topic");
		amqp.put("bindingVersion", AMQP_BINDING_VERSION);
	}

	private static void addOperation(final ObjectNode operation, final String channelId) {
		operation.put("action", "send");
		operation.putObject("channel").put("$ref", "#/channels/" + channelId);
		operation.putArray("messages").addObject().put("$ref", "#/channels/" + channelId + "/messages/" + MESSAGE);
	}
}
