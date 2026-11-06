package se.sundsvall.partyassets.api;

import com.turkraft.springfilter.parser.InvalidSyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import se.sundsvall.partyassets.integration.db.specification.InvalidFilterException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@ExtendWith(OutputCaptureExtension.class)
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
	void handleInvalidFilter(final CapturedOutput output) {
		final var exception = new InvalidFilterException(new IllegalArgumentException("Cannot compare left expression of type 'java.time.LocalDate'"));

		final var response = handler.handleInvalidFilter(exception);

		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(response.getHeaders().getContentType()).isEqualTo(APPLICATION_PROBLEM_JSON);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getBody().getTitle()).isEqualTo("Invalid Filter Content");
		assertThat(response.getBody().getDetail()).isEqualTo("The filter could not be applied to the assets.");
		assertThat(output).contains("WARN", "A filter that passed validation could not be applied to the assets", "Cannot compare left expression of type 'java.time.LocalDate'");
	}
}
