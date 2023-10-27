package se.sundsvall.partyassets.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.partyassets.Application;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.service.StatusService;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class MetadataStatusReasonResourceTest {

	@MockBean
	private StatusService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getReasonsForAllStatuses() {
		// Arrange
		final var statusReasons = Map.of(Status.BLOCKED, List.of("REASON_1", "REASON_2", "REASON_3"), Status.EXPIRED, List.of("REASON_3", "REASON_4"));
		
		when(serviceMock.getReasonsForAllStatuses()).thenReturn(statusReasons);
		
		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/metadata/statusreasons").build())
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentType(APPLICATION_JSON)
			.expectBody(new ParameterizedTypeReference<Map<Status, List<String>>>() {})
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isEqualTo(statusReasons);
		verify(serviceMock).getReasonsForAllStatuses();
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getReasonsForStatus() {
		// Arrange
		final var status = Status.EXPIRED;
		final var statusReasons = List.of("REASON_3", "REASON_4");

		when(serviceMock.getReasons(status)).thenReturn(statusReasons);

		// Act
		final var response = webTestClient.get()
			.uri("/metadata/statusreasons/{status}", status)
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentType(APPLICATION_JSON)
			.expectBody(new ParameterizedTypeReference<List<String>>() {})
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isEqualTo(statusReasons);
		verify(serviceMock).getReasons(status);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getReasonsForNonExistingStatus() {
		// Arrange
		final var expectedJsonMessage = """
			{
			  "title" : "Bad Request",
			  "status" : 400,
			  "detail" : "Failed to convert value of type 'java.lang.String' to required type 'se.sundsvall.partyassets.api.model.Status'; Failed to convert from type [java.lang.String] to type [@org.springframework.web.bind.annotation.PathVariable se.sundsvall.partyassets.api.model.Status] for value [BOGUS_STATUS]"
			}""";

		// Act
		webTestClient.get()
			.uri("/metadata/statusreasons/{status}", "BOGUS_STATUS")
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectHeader()
			.contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.json(expectedJsonMessage);
		;

		// Assert
		verifyNoInteractions(serviceMock);
	}

	@Test
	void createStatusReasons(@Value("${local.server.port}") int serverPort) {
		// Arrange
		final var status = Status.BLOCKED;
		final var body = List.of("REASON_1", "REASON_2", "REASON_3", "REASON_4");

		// Act
		webTestClient.post()
			.uri("/metadata/statusreasons/{status}", status)
			.bodyValue(body)
			.exchange()
			.expectStatus()
			.isCreated()
			.expectHeader()
			.location("http://localhost:" + serverPort + "/metadata/statusreasons/" + status);

		// Assert
		verify(serviceMock).createReasons(status, body);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createStatusReasonsForNonExistingStatus() {
		// Arrange
		final var body = List.of("REASON");
		final var expectedJsonMessage = """
			{
			  "title" : "Bad Request",
			  "status" : 400,
			  "detail" : "Failed to convert value of type 'java.lang.String' to required type 'se.sundsvall.partyassets.api.model.Status'; Failed to convert from type [java.lang.String] to type [@org.springframework.web.bind.annotation.PathVariable se.sundsvall.partyassets.api.model.Status] for value [BOGUS_STATUS]"
			}""";

		// Act
		webTestClient.post()
			.uri("/metadata/statusreasons/{status}", "BOGUS_STATUS")
			.bodyValue(body)
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectHeader()
			.contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.json(expectedJsonMessage);

		// Assert
		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = { " " })
	@NullAndEmptySource
	void createStatusReasonsFromInvalidStrings(String value) {
		// Arrange
		final var status = Status.BLOCKED;
		final var body = new ArrayList<>();
		final var expectedJsonMessage = """
			{
				"type" : "https://zalando.github.io/problem/constraint-violation",
				"status" : 400,
				"violations" : [ {
					"field" : "createReasons.statusReasons[0].<list element>",
					"message" : "must not be blank"
				} ],
				"title" : "Constraint Violation"
			}""";

		body.add(value);

		// Act
		webTestClient.post()
			.uri("/metadata/statusreasons/{status}", status)
			.bodyValue(body)
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectHeader()
			.contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.json(expectedJsonMessage);

		// Assert
		verifyNoInteractions(serviceMock);
	}

	@Test
	void createStatusReasonsWithEmptyList() {
		// Arrange
		final var status = Status.BLOCKED;
		final var body = new ArrayList<>();
		final var expectedJsonMessage = """
			{
				"type" : "https://zalando.github.io/problem/constraint-violation",
				"status" : 400,
				"violations" : [ {
					"field" : "createReasons.statusReasons",
					"message" : "must not be empty"
				} ],
				"title" : "Constraint Violation"
			}""";

		// Act
		webTestClient.post()
			.uri("/metadata/statusreasons/{status}", status)
			.bodyValue(body)
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectHeader()
			.contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.json(expectedJsonMessage);

		// Assert
		verifyNoInteractions(serviceMock);
	}

	@Test
	void deleteStatusReasons() {
		// Arrange
		final var status = Status.ACTIVE;

		// Act
		webTestClient.delete()
			.uri("/metadata/statusreasons/{status}", status)
			.exchange()
			.expectStatus()
			.isNoContent();

		// Assert
		verify(serviceMock).deleteReasons(status);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void deleteStatusReasonsForNonExistingStatus() {
		// Arrange
		final var expectedJsonMessage = """
			{
			  "title" : "Bad Request",
			  "status" : 400,
			  "detail" : "Failed to convert value of type 'java.lang.String' to required type 'se.sundsvall.partyassets.api.model.Status'; Failed to convert from type [java.lang.String] to type [@org.springframework.web.bind.annotation.PathVariable se.sundsvall.partyassets.api.model.Status] for value [BOGUS_STATUS]"
			}""";

		// Act
		webTestClient.delete()
			.uri("/metadata/statusreasons/{status}", "BOGUS_STATUS")
			.exchange()
			.expectStatus()
			.is4xxClientError()
			.expectBody()
			.json(expectedJsonMessage);

		// Assert
		verifyNoInteractions(serviceMock);
	}
}
