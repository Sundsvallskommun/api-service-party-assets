package se.sundsvall.partyassets.integration.db.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.TimeZoneStorage;

import static jakarta.persistence.FetchType.EAGER;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

@Entity
@IdClass(StatusEntityId.class)
@Table(name = "status")
public class StatusEntity {

	@Id
	private String name;

	@Id
	@Column(name = "municipality_id")
	private String municipalityId;

	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

	@ElementCollection(fetch = EAGER)
	@CollectionTable(name = "status_reason", joinColumns = {
		@JoinColumn(name = "status_name", referencedColumnName = "name"),
		@JoinColumn(name = "municipality_id", referencedColumnName = "municipality_id")
	}, foreignKey = @ForeignKey(name = "fk_status_reason_status"), indexes = {
		@Index(name = "idx_status_reason_status_name", columnList = "status_name, municipality_id")
	})
	@Column(name = "reason", nullable = false)
	private List<String> reasons;

	public static StatusEntity create() {
		return new StatusEntity();
	}

	@PrePersist
	void prePersist() {
		created = now(systemDefault()).truncatedTo(MILLIS);
	}

	@PreUpdate()
	void preUpdate() {
		updated = now(systemDefault()).truncatedTo(MILLIS);
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public StatusEntity withName(final String name) {
		this.name = name;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public StatusEntity withMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(final OffsetDateTime created) {
		this.created = created;
	}

	public StatusEntity withCreated(final OffsetDateTime created) {
		this.created = created;
		return this;
	}

	public OffsetDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(final OffsetDateTime updated) {
		this.updated = updated;
	}

	public StatusEntity withUpdated(final OffsetDateTime updated) {
		this.updated = updated;
		return this;
	}

	public List<String> getReasons() {
		return reasons;
	}

	public void setReasons(final List<String> reasons) {
		this.reasons = reasons;
	}

	public StatusEntity withReasons(final List<String> reasons) {
		this.reasons = reasons;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final StatusEntity that = (StatusEntity) o;
		return Objects.equals(name, that.name) && Objects.equals(municipalityId, that.municipalityId) && Objects.equals(created, that.created) && Objects.equals(updated, that.updated) && Objects.equals(reasons, that.reasons);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, municipalityId, created, updated, reasons);
	}

	@Override
	public String toString() {
		return "StatusEntity{" +
			"name='" + name + '\'' +
			", municipalityId='" + municipalityId + '\'' +
			", created=" + created +
			", updated=" + updated +
			", reasons=" + reasons +
			'}';
	}

}
