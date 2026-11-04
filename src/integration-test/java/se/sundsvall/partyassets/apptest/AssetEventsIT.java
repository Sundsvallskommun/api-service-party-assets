package se.sundsvall.partyassets.apptest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.integration.db.AssetRepository;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * The asset events against a real broker, with the exchange declared the way rabbitmq-topology declares it.
 *
 * @see src/test/resources/db/scripts/assetEventsIT.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/assetEventsIT/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/assetEventsIT.sql"
})
class AssetEventsIT extends AbstractAppTest {

	// The version in TEST and production
	private static final RabbitMQContainer BROKER = new RabbitMQContainer("rabbitmq:4.3.6-management-alpine");

	private static final String EXCHANGE = "api-fabriken.party-assets";
	private static final String QUEUE = "api-fabriken.party-assets.it";
	private static final String PATH = "/2281/assets";
	private static final String ASSET_ID = "0e4ab1c4-7d02-4d36-9c35-7a3d1bb0b39e";
	private static final String UUID_PATTERN = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
	private static final long RECEIVE_TIMEOUT_MILLIS = 5_000;
	private static final long SILENCE_TIMEOUT_MILLIS = 1_000;

	private static final RabbitAdmin ADMIN;
	private static final RabbitTemplate CONSUMER;

	static {
		BROKER.start();

		// The service never declares anything, so the test does it as rabbitmq-topology would
		final var connectionFactory = new CachingConnectionFactory(BROKER.getHost(), BROKER.getAmqpPort());
		connectionFactory.setUsername(BROKER.getAdminUsername());
		connectionFactory.setPassword(BROKER.getAdminPassword());
		ADMIN = new RabbitAdmin(connectionFactory);
		final var exchange = new TopicExchange(EXCHANGE);
		final var queue = QueueBuilder.durable(QUEUE).quorum().build();
		ADMIN.declareExchange(exchange);
		ADMIN.declareQueue(queue);
		ADMIN.declareBinding(BindingBuilder.bind(queue).to(exchange).with("asset.*"));
		CONSUMER = new RabbitTemplate(connectionFactory);
	}

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@DynamicPropertySource
	static void broker(final DynamicPropertyRegistry registry) {
		registry.add("dept44.rabbitmq.enabled", () -> true);
		registry.add("spring.rabbitmq.host", BROKER::getHost);
		registry.add("spring.rabbitmq.port", BROKER::getAmqpPort);
		registry.add("spring.rabbitmq.username", BROKER::getAdminUsername);
		registry.add("spring.rabbitmq.password", BROKER::getAdminPassword);
	}

	@BeforeEach
	void purge() {
		ADMIN.purgeQueue(QUEUE);
	}

	@Test
	void test01_createUpdateAndDelete() {
		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest("request.json")
			.withExpectedResponseStatus(CREATED)
			.sendRequest()
			.getResponseHeaders().getFirst(LOCATION);

		assertEvent(receive(), "asset.created", "created.json");

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(location)
			.withRequest("update.json")
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		assertEvent(receive(), "asset.updated", "updated.json");

		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(location)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		assertEvent(receive(), "asset.deleted", "deleted.json");
		assertThat(CONSUMER.receive(QUEUE, SILENCE_TIMEOUT_MILLIS)).isNull();
	}

	@Test
	void test02_rolledBackChange() {
		// Flushed, so the entity listener has seen the change, and then rolled back
		transactionTemplate.executeWithoutResult(status -> {
			assetRepository.findById(ASSET_ID).orElseThrow().setTitle("Rolled back");
			assetRepository.flush();
			status.setRollbackOnly();
		});
		// Flushed at commit, as when a service saves a change without flushing it
		transactionTemplate.executeWithoutResult(_ -> assetRepository.findById(ASSET_ID).orElseThrow().setTitle("Committed"));

		final var message = receive();
		assertThat(message.getMessageProperties().getReceivedRoutingKey()).isEqualTo("asset.updated");
		assertThatJson(body(message)).inPath("version").isEqualTo(1);
		assertThatJson(body(message)).inPath("asset.title").isEqualTo("Committed");
		assertThat(CONSUMER.receive(QUEUE, SILENCE_TIMEOUT_MILLIS)).isNull();
	}

	@Test
	void test03_republish() {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath("/2281/asset-events/republish")
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		// Only the asset of the municipality
		assertEvent(receive(), "asset.updated", "republished.json");
		assertThat(CONSUMER.receive(QUEUE, SILENCE_TIMEOUT_MILLIS)).isNull();
	}

	private Message receive() {
		final var message = CONSUMER.receive(QUEUE, RECEIVE_TIMEOUT_MILLIS);
		assertThat(message).as("A message on %s", QUEUE).isNotNull();
		return message;
	}

	private void assertEvent(final Message message, final String routingKey, final String expectedFile) {
		assertThat(message.getMessageProperties().getReceivedRoutingKey()).isEqualTo(routingKey);
		assertThat(message.getMessageProperties().getMessageId()).matches(UUID_PATTERN);
		assertThatJson(body(message)).isEqualTo(fromTestFile(expectedFile));
	}

	private static String body(final Message message) {
		return new String(message.getBody(), UTF_8);
	}
}
