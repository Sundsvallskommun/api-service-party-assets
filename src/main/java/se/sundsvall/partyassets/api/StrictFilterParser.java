package se.sundsvall.partyassets.api;

import com.turkraft.springfilter.parser.FilterParser;
import com.turkraft.springfilter.parser.InvalidSyntaxException;
import com.turkraft.springfilter.parser.ParseContext;
import com.turkraft.springfilter.parser.node.FilterNode;
import org.jspecify.annotations.Nullable;

/**
 * Parses filters with another parser, and reports a function, placeholder or operator that does not exist as a syntax
 * error, so that it is answered with a 400 Bad Request like any other filter that cannot be parsed.
 */
class StrictFilterParser implements FilterParser {

	private final FilterParser parser;

	StrictFilterParser(final FilterParser parser) {
		this.parser = parser;
	}

	@Override
	public FilterNode parse(final String input, @Nullable final ParseContext context) {
		try {
			return parser.parse(input, context);
		} catch (final UnsupportedOperationException exception) {
			throw new InvalidSyntaxException(exception.getMessage(), 0, 0, null, exception);
		}
	}
}
