package se.sundsvall.partyassets.integration.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.partyassets.integration.db.model.AssetEntity;

@CircuitBreaker(name = "assetRepository")
public interface AssetRepository extends JpaRepository<AssetEntity, String>, JpaSpecificationExecutor<AssetEntity> {
	Optional<AssetEntity> findByAssetId(String assetId);

	boolean existsByAssetId(String assetId);
}
