package se.sundsvall.partyassets.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.StatusRepository;
import se.sundsvall.partyassets.integration.db.model.StatusEntity;
import se.sundsvall.partyassets.integration.db.model.StatusEntityId;

import static java.util.Collections.emptyList;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.partyassets.service.mapper.StatusMapper.toEntity;
import static se.sundsvall.partyassets.service.mapper.StatusMapper.toReasons;

@Service
public class StatusService {

	private final StatusRepository repository;

	public StatusService(final StatusRepository repository) {
		this.repository = repository;
	}

	public Map<Status, List<String>> getReasonsForAllStatuses(final String municipalityId) {
		return toReasons(repository.findAllByMunicipalityId(municipalityId));
	}

	public List<String> getReasons(final String municipalityId, final Status status) {
		return repository.findByNameAndMunicipalityId(status.name(), municipalityId)
			.map(StatusEntity::getReasons)
			.orElse(emptyList());
	}

	public void createReasons(final String municipalityId, final Status status, final List<String> statusReasons) {
		if (repository.existsByNameAndMunicipalityId(status.name(), municipalityId)) {
			throw Problem.valueOf(CONFLICT, "Statusreasons already exists for status %s".formatted(status.name()));
		}
		repository.save(toEntity(status, statusReasons, municipalityId));
	}

	public void deleteReasons(final String municipalityId, final Status status) {
		if (!repository.existsByNameAndMunicipalityId(status.name(), municipalityId)) {
			throw Problem.valueOf(NOT_FOUND, "Status %s does not have any statusreasons to delete".formatted(status.name()));
		}
		repository.deleteById(new StatusEntityId(status.name(), municipalityId));
	}

}
