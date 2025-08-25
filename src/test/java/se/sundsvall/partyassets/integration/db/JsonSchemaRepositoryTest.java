package se.sundsvall.partyassets.integration.db;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.partyassets.integration.db.model.JsonSchemaEntity;

/**
 * JsonSchema repository tests.
 *
 * @see src/test/resources/db/scripts/JsonSchemaRepositoryTest.sql for data setup.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/jsonSchemaRepositoryTest.sql"
})
@Transactional(propagation = NOT_SUPPORTED)
class JsonSchemaRepositoryTest {

	private static final String ID_OF_JSON_SCHEMA = "2281_schema_1.0.0";
	private static final String ID_OF_JSON_SCHEMA_WITH_REFERENCES = "2281_schema_with_references_1.0.0";

	@Autowired
	private JsonSchemaRepository repository;

	@Test
	void testCreate() {

		// Arrange
		final var entity = JsonSchemaEntity.create()
			.withDescription("description")
			.withId(randomUUID().toString())
			.withMunicipalityId("2281")
			.withName("name")
			.withValue("{}")
			.withVersion("1.0");

		// Act
		final var persistedEntity = repository.save(entity);

		// Assert
		assertThat(repository.findById(entity.getId())).isPresent();
		assertThat(persistedEntity).isNotSameAs(entity);
		assertThat(persistedEntity).usingRecursiveComparison().ignoringFields("created").isEqualTo(entity);
		assertThat(persistedEntity.getCreated()).isCloseTo(now(), within(2, SECONDS));
	}

	@Test
	void findById() {

		// Act
		final var persistedEntity = repository.findById(ID_OF_JSON_SCHEMA);

		// Assert
		assertThat(persistedEntity).isPresent();
	}

	@Test
	void update() {

		// Arrange
		final var persistedEntity = repository.findById(ID_OF_JSON_SCHEMA).orElseThrow();
		persistedEntity.withDescription("new desription");

		// Act
		final var updatedEntity = repository.save(persistedEntity);

		// Assert
		assertThat(updatedEntity).isNotSameAs(persistedEntity);
		assertThat(updatedEntity).usingRecursiveComparison().isEqualTo(persistedEntity);
	}

	@Test
	void deleteById() {

		// Arrange
		assertThat(repository.findById(ID_OF_JSON_SCHEMA)).isPresent();

		// Act
		repository.deleteById(ID_OF_JSON_SCHEMA);

		// Assert
		assertThat(repository.findById(ID_OF_JSON_SCHEMA)).isEmpty();
	}

	@Test
	void deleteByIdWhenSchemaHasReferences() {

		// Arrange
		assertThat(repository.findById(ID_OF_JSON_SCHEMA_WITH_REFERENCES)).isPresent();

		// Act
		final var exception = assertThrows(DataIntegrityViolationException.class, () -> repository.deleteById(ID_OF_JSON_SCHEMA_WITH_REFERENCES));

		// Assert
		assertThat(exception.getMessage()).contains("Cannot delete or update a parent row");
	}
}
