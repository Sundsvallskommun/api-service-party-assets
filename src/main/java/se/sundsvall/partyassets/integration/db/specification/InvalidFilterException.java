package se.sundsvall.partyassets.integration.db.specification;

import java.io.Serial;

/**
 * Thrown when a filter from a client cannot be turned into a condition on the asset entity.
 */
public class InvalidFilterException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public InvalidFilterException(final Throwable cause) {
		super(cause);
	}
}
