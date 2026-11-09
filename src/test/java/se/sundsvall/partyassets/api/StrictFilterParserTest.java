package se.sundsvall.partyassets.api;

import com.turkraft.springfilter.parser.FilterParser;
import com.turkraft.springfilter.parser.InvalidSyntaxException;
import com.turkraft.springfilter.parser.ParseContext;
import com.turkraft.springfilter.parser.node.FieldNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StrictFilterParserTest {

	private static final String INPUT = "type : 'PERMIT'";

	@Mock
	private FilterParser parserMock;

	@Mock
	private ParseContext contextMock;

	private StrictFilterParser parser;

	@BeforeEach
	void setUp() {
		parser = new StrictFilterParser(parserMock);
	}

	@Test
	void parse() {
		final var node = new FieldNode("type");
		when(parserMock.parse(INPUT, contextMock)).thenReturn(node);

		assertThat(parser.parse(INPUT, contextMock)).isSameAs(node);
	}

	@Test
	void parseWithUnknownFunction() {
		final var cause = new UnsupportedOperationException("Unrecognized function `foo`");
		when(parserMock.parse(INPUT, contextMock)).thenThrow(cause);

		assertThatExceptionOfType(InvalidSyntaxException.class)
			.isThrownBy(() -> parser.parse(INPUT, contextMock))
			.withMessage("Unrecognized function `foo`")
			.withCause(cause);
	}

	@Test
	void parseWithSyntaxError() {
		final var syntaxError = new InvalidSyntaxException("mismatched input '<EOF>'", 1, 7, null, null);
		when(parserMock.parse(INPUT, null)).thenThrow(syntaxError);

		assertThatExceptionOfType(InvalidSyntaxException.class)
			.isThrownBy(() -> parser.parse(INPUT))
			.isSameAs(syntaxError);
	}
}
