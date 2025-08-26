package se.sundsvall.partyassets.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

@CircuitBreaker(name = "assetRepository")
public interface AssetRepository extends JpaRepository<AssetEntity, String>, JpaSpecificationExecutor<AssetEntity> {

	boolean existsByIdAndMunicipalityId(String id, String municipalityId);

	Optional<AssetEntity> findByIdAndMunicipalityId(String id, String municipalityId);

	boolean existsByAssetIdAndMunicipalityId(String assetId, String municipalityId);

	long countByJsonParametersSchemaId(String schemaId);
}
