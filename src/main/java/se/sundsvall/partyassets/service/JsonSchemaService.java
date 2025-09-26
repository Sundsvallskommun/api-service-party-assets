package se.sundsvall.partyassets.service;

import static java.util.Comparator.comparing;
import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.partyassets.service.Constants.JSON_SCHEMA_ALREADY_EXISTS;
import static se.sundsvall.partyassets.service.Constants.JSON_SCHEMA_REFERENCED_ASSETS;
import static se.sundsvall.partyassets.service.Constants.JSON_SCHEMA_WITH_GREATER_VERSION_EXISTS;
import static se.sundsvall.partyassets.service.Constants.MESSAGE_JSON_SCHEMA_NOT_FOUND_BY_ID;
import static se.sundsvall.partyassets.service.Constants.MESSAGE_JSON_SCHEMA_NOT_FOUND_BY_NAME;
import static se.sundsvall.partyassets.service.mapper.JsonSchemaMapper.toJsonSchema;
import static se.sundsvall.partyassets.service.mapper.JsonSchemaMapper.toJsonSchemaList;

import java.util.List;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.partyassets.api.model.JsonSchema;
import se.sundsvall.partyassets.api.model.JsonSchemaCreateRequest;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.db.JsonSchemaRepository;
import se.sundsvall.partyassets.service.mapper.JsonSchemaMapper;

@Service
public class JsonSchemaService {

	private final JsonSchemaRepository jsonSchemaRepository;
	private final AssetRepository assetRepository;

	public JsonSchemaService(JsonSchemaRepository jsonSchemaRepository, AssetRepository assetRepository) {
		this.jsonSchemaRepository = jsonSchemaRepository;
		this.assetRepository = assetRepository;
	}

	/**
	 * Get all schemas by municipality ID, enriched with number of references.
	 *
	 * @param  municipalityId the municipality ID
	 * @return                a list of {@link JsonSchema}
	 */
	public List<JsonSchema> getSchemas(String municipalityId) {
		return toJsonSchemaList(jsonSchemaRepository.findAllByMunicipalityId(municipalityId)).stream()
			.map(jsonSchema -> jsonSchema.withNumberOfReferences(assetRepository.countByJsonParametersSchemaId(jsonSchema.getId())))
			.toList();
	}

	/**
	 * Get schema by municipality ID and schema ID, enriched with number of references.
	 *
	 * @param  municipalityId                       the municipality ID
	 * @param  id                                   the schema ID
	 * @return                                      a {@link JsonSchema}
	 * @throws org.zalando.problem.ThrowableProblem if not found
	 */
	public JsonSchema getSchema(String municipalityId, String id) {
		return jsonSchemaRepository.findByMunicipalityIdAndId(municipalityId, id)
			.map(JsonSchemaMapper::toJsonSchema)
			.map(jsonSchema -> jsonSchema.withNumberOfReferences(assetRepository.countByJsonParametersSchemaId(jsonSchema.getId())))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, MESSAGE_JSON_SCHEMA_NOT_FOUND_BY_ID.formatted(id)));
	}

	/**
	 * Get latest schema by municipality ID and schema name, enriched with number of references.
	 *
	 * @param  municipalityId                       the municipality ID
	 * @param  name                                 the schema name
	 * @return                                      a {@link JsonSchema}
	 * @throws org.zalando.problem.ThrowableProblem if not found
	 */
	public JsonSchema getLatestSchemaByName(String municipalityId, String name) {
		return jsonSchemaRepository.findAllByMunicipalityIdAndName(municipalityId, name).stream()
			.max(comparing(obj -> new ComparableVersion(obj.getVersion())))
			.map(JsonSchemaMapper::toJsonSchema)
			.map(jsonSchema -> jsonSchema.withNumberOfReferences(assetRepository.countByJsonParametersSchemaId(jsonSchema.getId())))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, MESSAGE_JSON_SCHEMA_NOT_FOUND_BY_NAME.formatted(name)));
	}

	/**
	 * Create new schema or a new version of an existing schema.
	 *
	 * @param  municipalityId                       the municipality ID
	 * @param  request                              the schema request
	 * @return                                      the created {@link JsonSchema}
	 * @throws org.zalando.problem.ThrowableProblem if a conflicting schema already exists
	 */
	public JsonSchema create(String municipalityId, JsonSchemaCreateRequest request) {
		final var schemaEntity = JsonSchemaMapper.toJsonSchemaEntity(municipalityId, request);

		validateSchemaDoesNotAlreadyExist(schemaEntity.getId());
		validateNoGreaterVersionExists(municipalityId, request);

		// All good! Create schema.
		return toJsonSchema(jsonSchemaRepository.save(schemaEntity));
	}

	/**
	 * Delete an existing schema if it has no referencing assets.
	 *
	 * @param  municipalityId                       the municipality ID
	 * @param  id                                   the schema ID
	 * @throws org.zalando.problem.ThrowableProblem if not found or referenced
	 */
	public void delete(String municipalityId, String id) {
		final var schemaToDelete = getSchema(municipalityId, id);

		if (schemaToDelete.getNumberOfReferences() > 0) {
			throw Problem.valueOf(CONFLICT, JSON_SCHEMA_REFERENCED_ASSETS.formatted(schemaToDelete.getNumberOfReferences()));
		}

		jsonSchemaRepository.deleteById(schemaToDelete.getId());
	}

	// ---- Private helpers ------------------------------------------------------

	private void validateSchemaDoesNotAlreadyExist(String schemaId) {
		if (jsonSchemaRepository.existsById(schemaId)) {
			throw Problem.valueOf(CONFLICT, JSON_SCHEMA_ALREADY_EXISTS.formatted(schemaId));
		}
	}

	private void validateNoGreaterVersionExists(String municipalityId, JsonSchemaCreateRequest request) {
		final var newVersion = new ComparableVersion(request.getVersion());

		jsonSchemaRepository.findAllByMunicipalityIdAndName(municipalityId, request.getName().toLowerCase()).stream()
			.filter(existing -> newVersion.compareTo(new ComparableVersion(existing.getVersion())) < 0)
			.findAny()
			.ifPresent(existing -> {
				throw Problem.valueOf(CONFLICT, JSON_SCHEMA_WITH_GREATER_VERSION_EXISTS.formatted(existing.getId()));
			});
	}
}
