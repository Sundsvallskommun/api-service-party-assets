package se.sundsvall.partyassets.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.partyassets.integration.db.model.StatusEntity;
import se.sundsvall.partyassets.integration.db.model.StatusEntityId;

@CircuitBreaker(name = "statusRepository")
public interface StatusRepository extends JpaRepository<StatusEntity, StatusEntityId> {

	List<StatusEntity> findAllByMunicipalityId(String municipalityId);

	boolean existsByNameAndMunicipalityId(String name, String municipalityId);

	Optional<StatusEntity> findByNameAndMunicipalityId(String name, String municipalityId);
}
