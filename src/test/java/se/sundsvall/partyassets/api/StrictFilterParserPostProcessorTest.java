package se.sundsvall.partyassets.api;

import com.turkraft.springfilter.parser.FilterParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StrictFilterParserPostProcessorTest {

	private final StrictFilterParserPostProcessor postProcessor = new StrictFilterParserPostProcessor();

	@Test
	void wrapsFilterParser() {
		final var parser = mock(FilterParser.class);

		assertThat(postProcessor.postProcessAfterInitialization(parser, "filterParser"))
			.isInstanceOf(StrictFilterParser.class)
			.hasFieldOrPropertyWithValue("parser", parser);
	}

	@Test
	void leavesOtherBeansAsTheyAre() {
		final var bean = new Object();

		assertThat(postProcessor.postProcessAfterInitialization(bean, "other")).isSameAs(bean);
	}
}
