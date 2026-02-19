package se.sundsvall.partyassets.api.validation.impl;

import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper;
import org.springframework.web.context.request.RequestContextHolder;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.service.StatusService;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.allNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

abstract class ValidStatusReasonConstraintValidator {

	private static final String ERROR_MESSAGE_TEMPLATE = "'%s' is not valid reason for status %s. Valid reasons are %s.";

	private static final String MUNICIPALITY_ID = "municipalityId";

	private final StatusService statusService;

	protected ValidStatusReasonConstraintValidator(final StatusService statusService) {
		this.statusService = statusService;
	}

	boolean noStatusReason(final Status status, final String statusReason) {
		if (allNull(status, statusReason)) {
			return true;
		}
		final var municipalityId = getMunicipalityIdFromPath();
		if (municipalityId.isEmpty()) {
			return true;
		}

		return !statusService.getReasonsForAllStatuses(municipalityId.get()).containsKey(status) && isNull(statusReason);
	}

	/**
	 * TODO: Remove this and use the DEPT-44 version instead when that is implemented (UF-8954)
	 * Getting value for path variable name from current request
	 *
	 * @return an Optional containing the municipalityId, or empty if not in an HTTP request context
	 */
	Optional<String> getMunicipalityIdFromPath() {
		return Stream.ofNullable(RequestContextHolder.getRequestAttributes())
			.map(req -> req.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, SCOPE_REQUEST))
			.filter(Objects::nonNull)
			.filter(Map.class::isInstance)
			.map(Map.class::cast)
			.map(map -> map.get(MUNICIPALITY_ID))
			.filter(Objects::nonNull)
			.filter(String.class::isInstance)
			.map(String.class::cast)
			.findAny();
	}

	boolean isValidStatusReason(final Status status, final String statusReason) {
		if (allNull(status, statusReason)) {
			return true;
		}
		final var municipalityId = getMunicipalityIdFromPath();
		if (municipalityId.isEmpty()) {
			return true;
		}

		return isNotEmpty(statusReason) &&
			statusService.getReasonsForAllStatuses(municipalityId.get()).getOrDefault(status, emptyList()).contains(statusReason);
	}

	void useCustomMessageForValidation(final ConstraintValidatorContext constraintContext, final Status status, final String statusReason) {
		final var municipalityId = getMunicipalityIdFromPath();
		if (municipalityId.isEmpty()) {
			return;
		}

		constraintContext.disableDefaultConstraintViolation();
		constraintContext.buildConstraintViolationWithTemplate(InterpolationHelper.escapeMessageParameter(
			String.format(ERROR_MESSAGE_TEMPLATE,
				statusReason,
				status,
				statusService.getReasonsForAllStatuses(municipalityId.get()).getOrDefault(status, emptyList()))))
			.addConstraintViolation();
	}

}
