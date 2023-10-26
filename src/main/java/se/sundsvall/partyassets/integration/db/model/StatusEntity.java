package se.sundsvall.partyassets.integration.db.model;

import static jakarta.persistence.FetchType.EAGER;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "status")
public class StatusEntity {

	@Id
	private String name;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime created;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime updated;

	public static StatusEntity create() {
		return new StatusEntity();
	}

	@ElementCollection(fetch = EAGER)
	@CollectionTable(name = "status_reason", joinColumns = @JoinColumn(name = "status_name", referencedColumnName = "name", foreignKey = @ForeignKey(name = "fk_status_reason_status_name")), indexes = {
		@Index(name = "idx_status_reason_status_name", columnList = "status_name")
	})
	@Column(name = "reason", nullable = false)
	private List<String> reasons;

	@PrePersist
	void prePersist() {
		created = now(ZoneId.systemDefault()).truncatedTo(MILLIS);
	}

	@PreUpdate()
	void preUpdate() {
		updated = now(ZoneId.systemDefault()).truncatedTo(MILLIS);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public StatusEntity withName(String name) {
		this.name = name;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	public StatusEntity withCreated(OffsetDateTime created) {
		this.created = created;
		return this;
	}

	public OffsetDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(OffsetDateTime updated) {
		this.updated = updated;
	}

	public StatusEntity withUpdated(OffsetDateTime updated) {
		this.updated = updated;
		return this;
	}

	public List<String> getReasons() {
		return reasons;
	}

	public void setReasons(List<String> reasons) {
		this.reasons = reasons;
	}

	public StatusEntity withReasons(List<String> reasons) {
		this.reasons = reasons;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(created, name, reasons, updated);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof StatusEntity)) {
			return false;
		}
		StatusEntity other = (StatusEntity) obj;
		return Objects.equals(created, other.created) && Objects.equals(name, other.name) && Objects.equals(reasons, other.reasons) && Objects.equals(updated, other.updated);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StatusEntity [name=").append(name).append(", created=").append(created).append(", updated=").append(updated).append(", reasons=").append(reasons).append("]");
		return builder.toString();
	}
}
