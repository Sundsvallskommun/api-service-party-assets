package se.sundsvall.partyassets.api;

import com.turkraft.springfilter.parser.InvalidSyntaxException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

class FilterExceptionHandlerTest {

	private final FilterExceptionHandler handler = new FilterExceptionHandler();

	@Test
	void handleInvalidSyntax() {
		final var exception = new InvalidSyntaxException("mismatched input '<EOF>'", 0, 7, null, null);

		final var response = handler.handleInvalidSyntax(exception);

		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(response.getHeaders().getContentType()).isEqualTo(APPLICATION_PROBLEM_JSON);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getBody().getTitle()).isEqualTo("Invalid Filter Content");
		assertThat(response.getBody().getDetail()).isEqualTo(exception.getMessage());
	}

	@Test
	void handleInvalidFilter() {
		final var response = handler.handleInvalidFilter();

		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(response.getHeaders().getContentType()).isEqualTo(APPLICATION_PROBLEM_JSON);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getBody().getTitle()).isEqualTo("Invalid Filter Content");
		assertThat(response.getBody().getDetail()).isEqualTo("The filter could not be applied to the assets. Check that every operator suits the type of the attribute it is used on.");
	}
}
