package se.sundsvall.partyassets.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.api.model.JsonSchema;
import se.sundsvall.partyassets.api.model.JsonSchemaCreateRequest;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class MetadataJsonSchemaResourceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getSchemas() {

		// Arrange
		final var jsonSchemas = List.of(JsonSchema.create());

		// TODO: Mock service
		// when(serviceMock).thenReturn(something);

		// Act
		final var response = webTestClient.get()
			.uri("/{municipalityId}/metadata/jsonschemas", MUNICIPALITY_ID)
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(new ParameterizedTypeReference<List<JsonSchema>>() {}).returnResult().getResponseBody();

		// Assert
		assertThat(response).isEqualTo(jsonSchemas);

		// TODO: Verifications
		// verify(serviceMock).someMethod();
		// verifyNoMoreInteractions(serviceMock);
	}

	void getSchema() {

		// Arrange
		final var id = "some-schema-id";
		final var jsonSchema = JsonSchema.create();

		// TODO: Mock service
		// when(serviceMock).thenReturn(something);

		// Act
		final var response = webTestClient.get()
			.uri("/{municipalityId}/metadata/jsonschemas/{id}", MUNICIPALITY_ID, id)
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(JsonSchema.class).returnResult().getResponseBody();

		// Assert
		assertThat(response).isEqualTo(jsonSchema);

		// TODO: Verifications
		// verify(serviceMock).someMethod();
		// verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createSchema() {

		// Arrange
		final var id = "some-schema-id";
		final var body = JsonSchemaCreateRequest.create()
			.withDescription("description")
			.withName("name")
			.withValue("{}")
			.withVersion("1.0");

		// Act
		webTestClient.post()
			.uri("/{municipalityId}/metadata/jsonschemas", MUNICIPALITY_ID)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().location("/" + MUNICIPALITY_ID + "/metadata/jsonschemas/" + id);

		// TODO: Verifications
		// verify(serviceMock).somemethod();
		// verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateSchema() {

		// Arrange
		final var id = "some-schema-id";
		final var body = JsonSchemaCreateRequest.create()
			.withDescription("description")
			.withName("name")
			.withValue("{}")
			.withVersion("1.0");

		// Act
		webTestClient.patch()
			.uri("/{municipalityId}/metadata/jsonschemas/{id}", MUNICIPALITY_ID, id)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent();

		// TODO: Verifications
		// verify(serviceMock).somemethod();
		// verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void deleteSchema() {

		// Arrange
		final var id = "some-schema-id";

		// Act
		webTestClient.delete()
			.uri("/{municipalityId}/metadata/jsonschemas/{id}", MUNICIPALITY_ID, id)
			.exchange()
			.expectStatus().isNoContent();

		// TODO: Add verifications
		// verify(serviceMock).somemethod();
		// verifyNoMoreInteractions(serviceMock);
	}
}
