package se.sundsvall.partyassets.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.web.context.request.RequestContextHolder;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.partyassets.api.model.AssetJsonParameter;
import se.sundsvall.partyassets.api.validation.ValidJsonParameter;
import se.sundsvall.partyassets.service.JsonSchemaValidationService;

import static java.util.Objects.isNull;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

public class ValidJsonParameterConstraintValidator implements ConstraintValidator<ValidJsonParameter, AssetJsonParameter> {

	private static final String MUNICIPALITY_ID = "municipalityId";

	private final JsonSchemaValidationService jsonSchemaValidationService;
	private boolean nullable;

	public ValidJsonParameterConstraintValidator(JsonSchemaValidationService jsonSchemaValidationService) {
		this.jsonSchemaValidationService = jsonSchemaValidationService;
	}

	@Override
	public void initialize(final ValidJsonParameter constraintAnnotation) {
		this.nullable = constraintAnnotation.nullable();
	}

	@Override
	public boolean isValid(final AssetJsonParameter assetJsonParameter, final ConstraintValidatorContext context) {
		if (isNull(assetJsonParameter)) {
			return handleNullValue(context);
		}

		try {
			final var municipalityId = getMunicipalityIdFromPath();
			jsonSchemaValidationService.validate(
				municipalityId,
				assetJsonParameter.getSchemaId(),
				assetJsonParameter.getValue());
			return true;
		} catch (ThrowableProblem e) {
			addViolation(context, extractOriginalMessage(e));
			return false;
		} catch (Exception e) {
			addViolation(context, e.getMessage());
			return false;
		}
	}

	/**
	 * Extract original error message from ThrowableProblem.
	 * ProblemErrorDecoder wraps the detail as: "{integration} error: {detail={detail}, status={status}, title={title}}"
	 * For non-wrapped problems (e.g. Problem.valueOf), falls back to getMessage() which returns "title: detail".
	 */
	private String extractOriginalMessage(ThrowableProblem e) {
		final var wrappedDetail = e.getDetail();
		if (wrappedDetail == null || wrappedDetail.isBlank()) {
			return "Validation failed";
		}
		final var detailStart = wrappedDetail.indexOf("detail=");
		final var statusStart = wrappedDetail.indexOf(", status=");
		final var titleStart = wrappedDetail.lastIndexOf("title=");
		if (detailStart >= 0 && statusStart > detailStart && titleStart > statusStart) {
			final var originalDetail = wrappedDetail.substring(detailStart + "detail=".length(), statusStart);
			final var originalTitle = wrappedDetail.substring(titleStart + "title=".length(), wrappedDetail.length() - 1);
			return originalTitle + ": " + originalDetail;
		}
		return e.getMessage();
	}

	/**
	 * Get municipalityId from request path variable.
	 * Same pattern as ValidStatusReasonConstraintValidator.
	 */
	private String getMunicipalityIdFromPath() {
		return Stream.ofNullable(RequestContextHolder.getRequestAttributes())
			.map(req -> req.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, SCOPE_REQUEST))
			.filter(Objects::nonNull)
			.filter(Map.class::isInstance)
			.map(Map.class::cast)
			.map(map -> map.get(MUNICIPALITY_ID))
			.filter(Objects::nonNull)
			.filter(String.class::isInstance)
			.map(String.class::cast)
			.findAny()
			.orElseThrow(() -> new IllegalStateException("municipalityId not found in request path"));
	}

	private boolean handleNullValue(ConstraintValidatorContext context) {
		if (nullable) {
			return true;
		}
		addViolation(context, "Value is null");
		return false;
	}

	private void addViolation(ConstraintValidatorContext context, String message) {
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
	}
}
