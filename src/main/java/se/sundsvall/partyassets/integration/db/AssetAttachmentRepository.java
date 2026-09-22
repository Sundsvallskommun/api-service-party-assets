package se.sundsvall.partyassets.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.sundsvall.partyassets.integration.db.model.AssetAttachmentEntity;

@CircuitBreaker(name = "assetAttachmentRepository")
public interface AssetAttachmentRepository extends JpaRepository<AssetAttachmentEntity, String> {

	// The queries are spelled out rather than derived from the method name: assetId here is the asset primary key, while
	// the same word in AssetRepository means AssetEntity.assetId, the external business id.
	@Query("""
		select a from AssetAttachmentEntity a
		where a.asset.id = :assetId
		and a.municipalityId = :municipalityId
		and a.deleted = false
		order by a.created, a.id
		""")
	List<AssetAttachmentEntity> findAllForAsset(
		@Param("assetId") String assetId,
		@Param("municipalityId") String municipalityId);

	@Query("""
		select a from AssetAttachmentEntity a
		where a.id = :id
		and a.asset.id = :assetId
		and a.municipalityId = :municipalityId
		and a.deleted = false
		""")
	Optional<AssetAttachmentEntity> findByIdForAsset(
		@Param("id") String id,
		@Param("assetId") String assetId,
		@Param("municipalityId") String municipalityId);

	@Query("""
		select a from AssetAttachmentEntity a
		where a.id = :id
		and a.asset.id = :assetId
		and a.municipalityId = :municipalityId
		""")
	Optional<AssetAttachmentEntity> findByIdForAssetIncludingDeleted(
		@Param("id") String id,
		@Param("assetId") String assetId,
		@Param("municipalityId") String municipalityId);
}
