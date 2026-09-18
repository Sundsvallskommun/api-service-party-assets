package se.sundsvall.partyassets.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.partyassets.api.validation.ValidAttachmentContentType;

import static java.util.Optional.ofNullable;

public class ValidAttachmentContentTypeConstraintValidator implements ConstraintValidator<ValidAttachmentContentType, MultipartFile> {

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		"application/pdf",
		"image/png",
		"image/jpeg",
		"image/tiff",
		"application/vnd.openxmlformats-officedocument.wordprocessingml.document");

	@Override
	public boolean isValid(final MultipartFile file, final ConstraintValidatorContext context) {
		if (file == null) {
			return true;
		}

		return ofNullable(file.getContentType())
			.map(ValidAttachmentContentTypeConstraintValidator::toTypeAndSubtype)
			.map(ALLOWED_CONTENT_TYPES::contains)
			.orElse(false);
	}

	// A media type is case insensitive and may carry parameters, so "Application/PDF" and "application/pdf; charset=x"
	// both name the type on the list. Parsing with MediaType is not an option: it rejects an invalid charset outright.
	private static String toTypeAndSubtype(final String contentType) {
		return contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
	}
}
