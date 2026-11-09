package se.sundsvall.partyassets.api;

import com.turkraft.springfilter.parser.FilterParser;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Wraps the filter parser of SpringFilter in a {@link StrictFilterParser}, so that every filter in a request is parsed
 * by it.
 */
@Component
class StrictFilterParserPostProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) {
		return bean instanceof final FilterParser parser ? new StrictFilterParser(parser) : bean;
	}
}
