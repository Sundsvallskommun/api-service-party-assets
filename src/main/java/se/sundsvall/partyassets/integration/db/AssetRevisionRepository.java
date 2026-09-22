package se.sundsvall.partyassets.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.partyassets.integration.db.model.AssetRevisionEntity;

@CircuitBreaker(name = "assetRevisionRepository")
public interface AssetRevisionRepository extends JpaRepository<AssetRevisionEntity, String> {

	// assetId here is the asset primary key, as on AssetRevisionEntity - not AssetEntity.assetId, which this table
	// calls externalAssetId.
	List<AssetRevisionEntity> findByAssetIdOrderByRevisionDesc(String assetId);

	Optional<AssetRevisionEntity> findByAssetIdAndRevision(String assetId, Integer revision);
}
