package se.sundsvall.citizenassets.integration.db;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.sundsvall.citizenassets.integration.db.model.AssetEntity;

public interface AssetRepository extends JpaRepository<AssetEntity, UUID> , JpaSpecificationExecutor<AssetEntity> {
}
