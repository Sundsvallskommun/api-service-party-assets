package se.sundsvall.partyassets.service;

import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.partyassets.service.Constants.MESSAGE_JSON_SCHEMA_ALREADY_EXISTS;
import static se.sundsvall.partyassets.service.Constants.MESSAGE_JSON_SCHEMA_NOT_ABLE_TO_DELETE_REFERENCED_SCHEMAS;
import static se.sundsvall.partyassets.service.Constants.MESSAGE_JSON_SCHEMA_NOT_FOUND;
import static se.sundsvall.partyassets.service.Constants.MESSAGE_JSON_SCHEMA_WITH_GREATER_VERSION_ALREADY_EXISTS;
import static se.sundsvall.partyassets.service.mapper.JsonSchemaMapper.toJsonSchema;
import static se.sundsvall.partyassets.service.mapper.JsonSchemaMapper.toJsonSchemaEntity;
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
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, MESSAGE_JSON_SCHEMA_NOT_FOUND.formatted(id)));
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
		final var entityToCreate = toJsonSchemaEntity(municipalityId, request);

		// Check if a schema with this ID already exists.
		if (jsonSchemaRepository.existsById(entityToCreate.getId())) {
			throw Problem.valueOf(CONFLICT, MESSAGE_JSON_SCHEMA_ALREADY_EXISTS.formatted(entityToCreate.getId()));
		}

		// Check if a schema with greater version already exists.
		final var versionToCreate = new ComparableVersion(request.getVersion());
		jsonSchemaRepository.findAllByMunicipalityIdAndName(municipalityId, request.getName().toLowerCase()).stream()
			.filter(jsonSchema -> versionToCreate.compareTo(new ComparableVersion(jsonSchema.getVersion())) < 0)
			.findAny()
			.ifPresent(jsonSchemaWithGreaterVersion -> {
				throw Problem.valueOf(CONFLICT, MESSAGE_JSON_SCHEMA_WITH_GREATER_VERSION_ALREADY_EXISTS.formatted(jsonSchemaWithGreaterVersion.getId()));
			});

		// All good! Create schema.
		return toJsonSchema(jsonSchemaRepository.save(entityToCreate));
	}

	/**
	 * Delete an existing schema if it has no referencing assets.
	 *
	 * @param  municipalityId                       the municipality ID
	 * @param  id                                   the schema ID
	 * @throws org.zalando.problem.ThrowableProblem if not found or referenced
	 */
	public void delete(String municipalityId, String id) {
		final var jsonSchemaToDelete = getSchema(municipalityId, id);

		if (jsonSchemaToDelete.getNumberOfReferences() > 0) {
			throw Problem.valueOf(CONFLICT, MESSAGE_JSON_SCHEMA_NOT_ABLE_TO_DELETE_REFERENCED_SCHEMAS.formatted(jsonSchemaToDelete.getNumberOfReferences()));
		}

		jsonSchemaRepository.deleteById(jsonSchemaToDelete.getId());
	}
}
