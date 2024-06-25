package se.sundsvall.partyassets.integration.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.partyassets.integration.db.model.StatusEntity;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@CircuitBreaker(name = "statusRepository")
public interface StatusRepository extends JpaRepository<StatusEntity, String> {

	List<StatusEntity> findAllByMunicipalityId(String municipalityId);

	boolean existsByNameAndMunicipalityId(String id, String municipalityId);

	Optional<StatusEntity> findByNameAndMunicipalityId(String id, String municipalityId);

}
