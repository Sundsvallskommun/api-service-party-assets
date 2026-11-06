package se.sundsvall.partyassets.api;

import com.turkraft.springfilter.parser.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ProblemResponse;
import se.sundsvall.partyassets.integration.db.specification.InvalidFilterException;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.http.ResponseEntity.badRequest;

/**
 * Turns a filter that cannot be parsed, or that cannot be applied to the assets, into a 400 Bad Request.
 */
@RestControllerAdvice
// Ahead of the dept44 handler, whose catch-all for Exception would otherwise answer with a 500
@Order(HIGHEST_PRECEDENCE)
class FilterExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(FilterExceptionHandler.class);

	private static final String TITLE = "Invalid Filter Content";
	private static final String INVALID_FILTER_DETAIL = "The filter could not be applied to the assets.";

	@ExceptionHandler
	ResponseEntity<Problem> handleInvalidSyntax(final InvalidSyntaxException exception) {
		return badRequestWithDetail(exception.getMessage());
	}

	@ExceptionHandler
	ResponseEntity<Problem> handleInvalidFilter(final InvalidFilterException exception) {
		// The validation of the filter lets through only filters that can be applied, so this points to a gap in the
		// validation or a fault in the mapping of the entity. The cause describes the entity and Hibernate rather than the
		// filter, so it is logged instead of being returned.
		LOG.warn("A filter that passed validation could not be applied to the assets", exception);
		return badRequestWithDetail(INVALID_FILTER_DETAIL);
	}

	private static ResponseEntity<Problem> badRequestWithDetail(final String detail) {
		final var problem = new ProblemResponse();
		problem.setStatus(BAD_REQUEST);
		problem.setTitle(TITLE);
		problem.setDetail(detail);

		return badRequest()
			.contentType(APPLICATION_PROBLEM_JSON)
			.body(problem);
	}
}
