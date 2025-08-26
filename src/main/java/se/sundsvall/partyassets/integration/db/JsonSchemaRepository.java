package se.sundsvall.partyassets.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.partyassets.integration.db.model.JsonSchemaEntity;

@CircuitBreaker(name = "jsonSchemaRepository")
public interface JsonSchemaRepository extends JpaRepository<JsonSchemaEntity, String> {

	Optional<JsonSchemaEntity> findByMunicipalityIdAndId(String municipalityId, String id);

	List<JsonSchemaEntity> findAllByMunicipalityId(String municipalityId);

	List<JsonSchemaEntity> findAllByMunicipalityIdAndName(String municipalityId, String name);
}
