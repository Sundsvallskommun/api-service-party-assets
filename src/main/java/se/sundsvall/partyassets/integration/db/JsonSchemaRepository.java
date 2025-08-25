package se.sundsvall.partyassets.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.partyassets.integration.db.model.JsonSchemaEntity;

@CircuitBreaker(name = "jsonSchemaRepository")
public interface JsonSchemaRepository extends JpaRepository<JsonSchemaEntity, String> {
}
