package se.sundsvall.citizenassets.integration.db.model;

import static java.time.LocalDateTime.now;

import java.sql.Timestamp;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@MappedSuperclass
abstract class BaseEntity {

	protected Timestamp created;
	protected Timestamp updated;

	@PrePersist
	void setCreated() {
		created = Timestamp.valueOf(now());
	}

	@PreUpdate()
	void setUpdated() {
		updated = Timestamp.valueOf(now());
	}
}
