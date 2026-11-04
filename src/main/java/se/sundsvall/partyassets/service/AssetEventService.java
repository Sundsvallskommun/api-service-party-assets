package se.sundsvall.partyassets.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.partyassets.integration.db.AssetRepository;
import se.sundsvall.partyassets.integration.events.AssetEventMapper;
import se.sundsvall.partyassets.integration.events.AssetEventPublisher;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

/**
 * Publishes every asset of a municipality again, to load a new consumer or to recover one.
 * <p>
 * Not transactional: each page is read in a transaction of its own and published after it, so a long run holds neither
 * a database connection nor a growing persistence context. An asset that changes meanwhile is published by the change
 * too, and its version tells the consumer which event is newer. The pages follow the id rather than an offset, so an
 * asset deleted meanwhile does not move another one past the next page unpublished.
 */
@Service
public class AssetEventService {

	private static final Logger LOG = LoggerFactory.getLogger(AssetEventService.class);

	private static final Limit PAGE_SIZE = Limit.of(100);
	private static final String RABBITMQ_OFF = "RabbitMQ is off, so no asset events can be published";

	private final AssetRepository repository;
	private final Optional<AssetEventPublisher> publisher;

	public AssetEventService(final AssetRepository repository, final Optional<AssetEventPublisher> publisher) {
		this.repository = repository;
		this.publisher = publisher;
	}

	public void republish(final String municipalityId) {
		final var eventPublisher = publisher.orElseThrow(() -> Problem.valueOf(SERVICE_UNAVAILABLE, RABBITMQ_OFF));

		var published = 0;
		var page = repository.findByMunicipalityIdAndIdGreaterThanOrderById(municipalityId, "", PAGE_SIZE);
		while (!page.isEmpty()) {
			page.stream()
				.map(AssetEventMapper::toRepublishedAssetEvent)
				.forEach(eventPublisher::publish);
			published += page.size();
			page = repository.findByMunicipalityIdAndIdGreaterThanOrderById(municipalityId, page.getLast().getId(), PAGE_SIZE);
		}
		LOG.info("Republished {} asset(s) of municipality {}", published, municipalityId);
	}
}
