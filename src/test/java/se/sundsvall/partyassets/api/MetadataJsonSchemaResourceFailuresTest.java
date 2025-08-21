package se.sundsvall.partyassets.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.zalando.problem.Status.BAD_REQUEST;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.api.model.JsonSchemaCreateRequest;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class MetadataJsonSchemaResourceFailuresTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getSchemasInvalidMunicipalityId() {

		// Act
		final var response = webTestClient.get()
			.uri("/invalid-municipality/metadata/jsonschemas")
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getSchemas.municipalityId", "not a valid municipality ID"));

		// TODO: Add verification
		// verifyNoInteractions(assetServiceMock);
	}

	@Test
	void getSchemaInvalidMunicipalityId() {

		// Arrange
		final var id = "some_schema";

		// Act
		final var response = webTestClient.get()
			.uri("/invalid-municipality/metadata/jsonschemas/{id}", id)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getSchemaById.municipalityId", "not a valid municipality ID"));

		// TODO: Add verification
		// verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createSchemaInvalidMunicipalityId() {

		// Arrange
		final var schemaRequest = JsonSchemaCreateRequest.create()
			.withDescription("description")
			.withName("name")
			.withValue("value")
			.withVersion("1.0");

		// Act
		final var response = webTestClient.post()
			.uri("/invalid-municipality/metadata/jsonschemas")
			.bodyValue(schemaRequest)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("createSchema.municipalityId", "not a valid municipality ID"));

		// TODO: Add verification
		// verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createSchemaEmptyRequestBody() {

		// Arrange
		final var schemaRequest = JsonSchemaCreateRequest.create();

		// Act
		final var response = webTestClient.post()
			.uri("/{municipalityId}/metadata/jsonschemas", MUNICIPALITY_ID)
			.bodyValue(schemaRequest)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(
				tuple("name", "must not be blank"),
				tuple("value", "must not be blank"),
				tuple("version", "must not be null"));

		// TODO: Add verification
		// verifyNoInteractions(assetServiceMock);
	}

	@Test
	void createSchemaInvalidVersion() {

		// Arrange
		final var schemaRequest = JsonSchemaCreateRequest.create()
			.withDescription("description")
			.withName("name")
			.withValue("value")
			.withVersion("invalid-version");

		// Act
		final var response = webTestClient.post()
			.uri("/{municipalityId}/metadata/jsonschemas", MUNICIPALITY_ID)
			.bodyValue(schemaRequest)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("version", "must match \"^(\\d+\\.)?(\\d+)$\""));

		// TODO: Add verification
		// verifyNoInteractions(assetServiceMock);
	}

	@Test
	void updateSchemaInvalidMunicipalityId() {

		// Arrange
		final var id = "some-id";
		final var schemaRequest = JsonSchemaCreateRequest.create()
			.withDescription("description")
			.withName("name")
			.withValue("value")
			.withVersion("1.0");

		// Act
		final var response = webTestClient.patch()
			.uri("/invalid-municipality/metadata/jsonschemas/{id}", id)
			.bodyValue(schemaRequest)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("updateSchema.municipalityId", "not a valid municipality ID"));

		// TODO: Add verification
		// verifyNoInteractions(assetServiceMock);
	}

	@Test
	void updateSchemaEmptyRequestBody() {

		// Arrange
		final var id = "some-id";
		final var schemaRequest = JsonSchemaCreateRequest.create();

		// Act
		final var response = webTestClient.patch()
			.uri("/{municipalityId}/metadata/jsonschemas/{id}", MUNICIPALITY_ID, id)
			.bodyValue(schemaRequest)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(
				tuple("name", "must not be blank"),
				tuple("value", "must not be blank"),
				tuple("version", "must not be null"));

		// TODO: Add verification
		// verifyNoInteractions(assetServiceMock);
	}

	@Test
	void updateSchemaInvalidVersion() {

		// Arrange
		final var id = "some-id";
		final var schemaRequest = JsonSchemaCreateRequest.create()
			.withDescription("description")
			.withName("name")
			.withValue("value")
			.withVersion("invalid-version");

		// Act
		final var response = webTestClient.patch()
			.uri("/{municipalityId}/metadata/jsonschemas/{id}", MUNICIPALITY_ID, id)
			.bodyValue(schemaRequest)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("version", "must match \"^(\\d+\\.)?(\\d+)$\""));

		// TODO: Add verification
		// verifyNoInteractions(assetServiceMock);
	}

	@Test
	void deleteSchemaInvalidMunicipalityId() {

		// Arrange
		final var id = "some_schema";

		// Act
		final var response = webTestClient.delete()
			.uri("/invalid-municipality/metadata/jsonschemas/{id}", id)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("deleteSchema.municipalityId", "not a valid municipality ID"));

		// TODO: Add verification
		// verifyNoInteractions(assetServiceMock);
	}
}
