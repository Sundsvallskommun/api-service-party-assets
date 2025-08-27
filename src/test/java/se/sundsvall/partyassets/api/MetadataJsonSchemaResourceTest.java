package se.sundsvall.partyassets.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.api.model.JsonSchema;
import se.sundsvall.partyassets.api.model.JsonSchemaCreateRequest;
import se.sundsvall.partyassets.service.JsonSchemaService;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class MetadataJsonSchemaResourceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@MockitoBean
	private JsonSchemaService jsonSchemaServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getSchemas() {

		// Arrange
		final var jsonSchemas = List.of(JsonSchema.create());
		when(jsonSchemaServiceMock.getSchemas(MUNICIPALITY_ID)).thenReturn(jsonSchemas);

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

		verify(jsonSchemaServiceMock).getSchemas(MUNICIPALITY_ID);
		verifyNoMoreInteractions(jsonSchemaServiceMock);
	}

	void getSchema() {

		// Arrange
		final var id = "some-schema-id";
		final var jsonSchema = JsonSchema.create();
		when(jsonSchemaServiceMock.getSchema(MUNICIPALITY_ID, id)).thenReturn(jsonSchema);

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

		verify(jsonSchemaServiceMock).getSchema(MUNICIPALITY_ID, id);
		verifyNoMoreInteractions(jsonSchemaServiceMock);
	}

	@Test
	void createSchema() {

		// Arrange
		final var id = "some-schema-id";
		final var body = JsonSchemaCreateRequest.create()
			.withDescription("description")
			.withName("name")
			.withValue("{\"$schema\": \"https://json-schema.org/draft/2020-12/schema\"}")
			.withVersion("1.0");

		when(jsonSchemaServiceMock.create(any(), any())).thenReturn(JsonSchema.create().withId(id));

		// Act
		webTestClient.post()
			.uri("/{municipalityId}/metadata/jsonschemas", MUNICIPALITY_ID)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().location("/" + MUNICIPALITY_ID + "/metadata/jsonschemas/" + id);

		// Assert
		verify(jsonSchemaServiceMock).create(MUNICIPALITY_ID, body);
		verifyNoMoreInteractions(jsonSchemaServiceMock);
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

		// Assert
		verify(jsonSchemaServiceMock).delete(MUNICIPALITY_ID, id);
		verifyNoMoreInteractions(jsonSchemaServiceMock);
	}
}
