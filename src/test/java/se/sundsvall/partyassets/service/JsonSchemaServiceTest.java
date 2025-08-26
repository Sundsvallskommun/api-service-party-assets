package se.sundsvall.partyassets.service;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.partyassets.TestFactory;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.JsonSchemaRepository;
import se.sundsvall.partyassets.integration.db.model.JsonSchemaEntity;

@ExtendWith(MockitoExtension.class)
class JsonSchemaServiceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private AssetRepository assetRepositoryMock;

	@Mock
	private JsonSchemaRepository jsonSchemaRepositoryMock;

	@Captor
	private ArgumentCaptor<JsonSchemaEntity> entityCaptor;

	@InjectMocks
	private JsonSchemaService service;

	@Test
	void getSchemas() {

		// Arrange
		final var entity = TestFactory.getJsonSchemaEntity();
		when(jsonSchemaRepositoryMock.findAllByMunicipalityId(MUNICIPALITY_ID)).thenReturn(List.of(entity));
		when(assetRepositoryMock.countByJsonParametersSchemaId(any())).thenReturn(5L);

		// Act
		final var result = service.getSchemas(MUNICIPALITY_ID);

		// Assert
		assertThat(result)
			.hasSize(1)
			.first()
			.usingRecursiveComparison()
			.ignoringFields("numberOfReferences") // always zero in entity
			.isEqualTo(entity);

		assertThat(result.getFirst().getNumberOfReferences()).isEqualTo(5L);

		verify(jsonSchemaRepositoryMock).findAllByMunicipalityId(MUNICIPALITY_ID);
		verify(assetRepositoryMock).countByJsonParametersSchemaId(entity.getId());
		verifyNoMoreInteractions(jsonSchemaRepositoryMock, assetRepositoryMock);
	}

	@Test
	void getSchema() {

		// Arrange
		final var entity = TestFactory.getJsonSchemaEntity();
		final var id = entity.getId();
		when(jsonSchemaRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, id)).thenReturn(Optional.of(entity));
		when(assetRepositoryMock.countByJsonParametersSchemaId(any())).thenReturn(5L);

		// Act
		final var result = service.getSchema(MUNICIPALITY_ID, id);

		// Assert
		assertThat(result)
			.usingRecursiveComparison()
			.ignoringFields("numberOfReferences") // always zero in entity
			.isEqualTo(entity);

		assertThat(result.getNumberOfReferences()).isEqualTo(5L);

		verify(jsonSchemaRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, id);
		verify(assetRepositoryMock).countByJsonParametersSchemaId(id);
		verifyNoMoreInteractions(jsonSchemaRepositoryMock, assetRepositoryMock);
	}

	@Test
	void createSchema() {

		// Arrange
		final var jsonSchemaCreateRequest = TestFactory.getJsonSchemaCreateRequest();
		final var entity = TestFactory.getJsonSchemaEntity();

		when(jsonSchemaRepositoryMock.existsById(any())).thenReturn(false);
		when(jsonSchemaRepositoryMock.findAllByMunicipalityIdAndName(any(), any())).thenReturn(emptyList());
		when(jsonSchemaRepositoryMock.save(any())).thenReturn(entity);

		// Act
		final var result = service.create(MUNICIPALITY_ID, jsonSchemaCreateRequest);

		// Assert
		assertThat(result)
			.usingRecursiveComparison()
			.ignoringFields("numberOfReferences") // always zero in entity
			.isEqualTo(entity);

		verify(jsonSchemaRepositoryMock).findAllByMunicipalityIdAndName(MUNICIPALITY_ID, jsonSchemaCreateRequest.getName());
		verify(jsonSchemaRepositoryMock).existsById("%s_%s_%s".formatted(MUNICIPALITY_ID, jsonSchemaCreateRequest.getName(), jsonSchemaCreateRequest.getVersion()));
		verify(jsonSchemaRepositoryMock).save(entityCaptor.capture());
		verifyNoMoreInteractions(jsonSchemaRepositoryMock, assetRepositoryMock);

		final var capturedValue = entityCaptor.getValue();
		assertThat(capturedValue.getCreated()).isNull();
		assertThat(capturedValue.getDescription()).isEqualTo(jsonSchemaCreateRequest.getDescription());
		assertThat(capturedValue.getId()).isEqualTo("%s_%s_%s".formatted(MUNICIPALITY_ID, jsonSchemaCreateRequest.getName(), jsonSchemaCreateRequest.getVersion()));
		assertThat(capturedValue.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(capturedValue.getName()).isEqualTo(jsonSchemaCreateRequest.getName());
		assertThat(capturedValue.getValue()).isEqualTo(jsonSchemaCreateRequest.getValue());
		assertThat(capturedValue.getVersion()).isEqualTo(jsonSchemaCreateRequest.getVersion());
	}

	@Test
	void createSchemaWhenVersionAlreadyExists() {

		// Arrange
		final var jsonSchemaCreateRequest = TestFactory.getJsonSchemaCreateRequest();

		when(jsonSchemaRepositoryMock.existsById(any())).thenReturn(true);

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> service.create(MUNICIPALITY_ID, jsonSchemaCreateRequest));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(CONFLICT);
		assertThat(exception.getMessage()).isEqualTo("Conflict: A JsonSchema already exists with ID '2281_person_schema_1.0'!");

		verify(jsonSchemaRepositoryMock).existsById("%s_%s_%s".formatted(MUNICIPALITY_ID, jsonSchemaCreateRequest.getName(), jsonSchemaCreateRequest.getVersion()));
		verifyNoMoreInteractions(jsonSchemaRepositoryMock, assetRepositoryMock);
	}

	@Test
	void createSchemaWhenGreaterVersionAlreadyExists() {

		// Arrange
		final var jsonSchemaCreateRequest = TestFactory.getJsonSchemaCreateRequest();
		assertThat(jsonSchemaCreateRequest.getVersion()).isEqualTo("1.0");

		when(jsonSchemaRepositoryMock.existsById(any())).thenReturn(false);
		when(jsonSchemaRepositoryMock.findAllByMunicipalityIdAndName(any(), any())).thenReturn(List.of(
			JsonSchemaEntity.create().withId("id-1").withVersion("0.4"),
			JsonSchemaEntity.create().withId("id-2").withVersion("1.4")));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> service.create(MUNICIPALITY_ID, jsonSchemaCreateRequest));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(CONFLICT);
		assertThat(exception.getMessage()).isEqualTo("Conflict: A JsonSchema with a greater version already exists! (see schema with ID: 'id-2')");

		verify(jsonSchemaRepositoryMock).existsById("%s_%s_%s".formatted(MUNICIPALITY_ID, jsonSchemaCreateRequest.getName(), jsonSchemaCreateRequest.getVersion()));
		verifyNoMoreInteractions(jsonSchemaRepositoryMock, assetRepositoryMock);
	}

	@Test
	void delete() {

		// Arrange
		final var id = "some-id";
		final var entityToDelete = JsonSchemaEntity.create().withId(id);

		when(jsonSchemaRepositoryMock.findByMunicipalityIdAndId(any(), any())).thenReturn(Optional.of(entityToDelete));

		// Act
		service.delete(MUNICIPALITY_ID, id);

		// Assert
		verify(jsonSchemaRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, id);
		verify(jsonSchemaRepositoryMock).delete(entityToDelete);
		verifyNoMoreInteractions(jsonSchemaRepositoryMock, assetRepositoryMock);
	}

	@Test
	void deleteWhenNotFound() {

		// Arrange
		final var id = "some-id";

		when(jsonSchemaRepositoryMock.findByMunicipalityIdAndId(any(), any())).thenReturn(Optional.empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> service.delete(MUNICIPALITY_ID, id));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(exception.getMessage()).isEqualTo("Not Found: A JsonSchema with ID 'some-id' was not found for municipalityId '2281'");

		verify(jsonSchemaRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, id);
		verifyNoMoreInteractions(jsonSchemaRepositoryMock, assetRepositoryMock);
	}
}
