package se.sundsvall.partyassets.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.partyassets.TestFactory;

class JsonSchemaMapperTest {

	@Test
	void toJsonSchema() {

		// Arrange
		final var entity = TestFactory.getJsonSchemaEntity();

		// Act
		final var result = JsonSchemaMapper.toJsonSchema(entity);

		// Assert
		assertThat(result)
			.usingRecursiveComparison()
			.ignoringFields("numberOfReferences") // always zero
			.isEqualTo(entity);

		assertThat(result.getNumberOfReferences()).isZero();
	}

	@Test
	void toJsonSchemaWhenInputIsNull() {

		// Act
		final var result = JsonSchemaMapper.toJsonSchema(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	void toJsonSchemaList() {

		// Arrange
		final var entity = TestFactory.getJsonSchemaEntity();
		final var entityList = List.of(entity);

		// Act
		final var result = JsonSchemaMapper.toJsonSchemaList(entityList);

		// Assert
		assertThat(result)
			.hasSize(1)
			.first()
			.usingRecursiveComparison()
			.ignoringFields("numberOfReferences") // always zero
			.isEqualTo(entity);

		assertThat(result.getFirst().getNumberOfReferences()).isZero();
	}

	@Test
	void toJsonSchemaListWhenInputIsNull() {

		// Act
		final var result = JsonSchemaMapper.toJsonSchemaList(null);

		// Assert
		assertThat(result).isEmpty();
	}

	@Test
	void toJsonSchemaEntity() {

		// Arrange
		final var municipalityId = "2281";
		final var jsonSchemaCreateRequest = TestFactory.getJsonSchemaCreateRequest();

		// Act
		final var result = JsonSchemaMapper.toJsonSchemaEntity(municipalityId, jsonSchemaCreateRequest);

		// Assert
		assertThat(result.getCreated()).isNull();
		assertThat(result.getDescription()).isEqualTo(jsonSchemaCreateRequest.getDescription());
		assertThat(result.getId())
			.isEqualToIgnoringCase("%s_%s_%s".formatted(municipalityId, jsonSchemaCreateRequest.getName(), jsonSchemaCreateRequest.getVersion()))
			.isLowerCase();
		assertThat(result.getName())
			.isEqualToIgnoringCase(jsonSchemaCreateRequest.getName())
			.isLowerCase();
		assertThat(result.getValue()).isEqualTo(jsonSchemaCreateRequest.getValue());
		assertThat(result.getVersion()).isEqualTo(jsonSchemaCreateRequest.getVersion());
	}
}
