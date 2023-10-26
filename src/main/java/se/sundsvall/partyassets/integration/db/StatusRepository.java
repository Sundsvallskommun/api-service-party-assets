package se.sundsvall.partyassets.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.partyassets.integration.db.model.StatusEntity;

@CircuitBreaker(name = "statusRepository")
public interface StatusRepository extends JpaRepository<StatusEntity, String> {}
