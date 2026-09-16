package se.sundsvall.partyassets.api.validation.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class ValidAttachmentContentTypeConstraintValidatorTest {

	private final ValidAttachmentContentTypeConstraintValidator validator = new ValidAttachmentContentTypeConstraintValidator();

	@ParameterizedTest
	@ValueSource(strings = {
		"application/pdf", "image/png", "image/jpeg", "image/tiff", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
		"Application/PDF", "application/pdf; charset=binary", "IMAGE/TIFF"
	})
	void validContentTypes(final String contentType) {
		final var file = new MockMultipartFile("attachment", "file", contentType, "content".getBytes());

		assertThat(validator.isValid(file, null)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"application/octet-stream", "text/html", "application/zip", "not a media type"
	})
	void invalidContentTypes(final String contentType) {
		final var file = new MockMultipartFile("attachment", "file", contentType, "content".getBytes());

		assertThat(validator.isValid(file, null)).isFalse();
	}

	@Test
	void missingContentType() {
		final var file = new MockMultipartFile("attachment", "file", null, "content".getBytes());

		assertThat(validator.isValid(file, null)).isFalse();
	}

	@Test
	void nullFile() {
		assertThat(validator.isValid(null, null)).isTrue();
	}
}
