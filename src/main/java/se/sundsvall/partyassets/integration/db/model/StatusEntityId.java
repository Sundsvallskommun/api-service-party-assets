package se.sundsvall.partyassets.integration.db.model;

import java.io.Serializable;
import java.util.Objects;

public class StatusEntityId implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String municipalityId;

	public StatusEntityId() {}

	public StatusEntityId(final String name, final String municipalityId) {
		this.name = name;
		this.municipalityId = municipalityId;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final StatusEntityId that = (StatusEntityId) o;
		return Objects.equals(name, that.name) && Objects.equals(municipalityId, that.municipalityId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, municipalityId);
	}

}
