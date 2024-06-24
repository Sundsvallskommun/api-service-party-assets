package se.sundsvall.partyassets.service;

import static java.util.Collections.emptyList;
import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.partyassets.service.mapper.StatusMapper.toEntity;
import static se.sundsvall.partyassets.service.mapper.StatusMapper.toReasons;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.StatusRepository;
import se.sundsvall.partyassets.integration.db.model.StatusEntity;

@Service
public class StatusService {

	private static final String CACHE_NAME = "statusReasonCache";

	private final StatusRepository repository;

	public StatusService(final StatusRepository repository) {
		this.repository = repository;
	}

	@Cacheable(value = CACHE_NAME, key = "{#root.methodName}")
	public Map<Status, List<String>> getReasonsForAllStatuses(final String municipalityId) {
		return toReasons(repository.findAll());
	}

	@Cacheable(value = CACHE_NAME, key = "{#root.methodName, #status}")
	public List<String> getReasons(final String municipalityId, final Status status) {
		return repository.findById(status.name())
			.map(StatusEntity::getReasons)
			.orElse(emptyList());
	}

	@Caching(evict = {
		@CacheEvict(value = CACHE_NAME, key = "{'getReasonsForAllStatuses'}"),
		@CacheEvict(value = CACHE_NAME, key = "{'getReasons', #status}")
	})
	public void createReasons(final String municipalityId, final Status status, final List<String> statusReasons) {
		if (repository.existsById(status.name())) {
			throw Problem.valueOf(CONFLICT, "Statusreasons already exists for status %s".formatted(status.name()));
		}
		repository.save(toEntity(status, statusReasons));
	}

	@Caching(evict = {
		@CacheEvict(value = CACHE_NAME, key = "{'getReasonsForAllStatuses'}"),
		@CacheEvict(value = CACHE_NAME, key = "{'getReasons', #status}")
	})
	public void deleteReasons(final String municipalityId,final Status status) {
		if (!repository.existsById(status.name())) {
			throw Problem.valueOf(NOT_FOUND, "Status %s does not have any statusreasons to delete".formatted(status.name()));
		}
		repository.deleteById(status.name());
	}
}
