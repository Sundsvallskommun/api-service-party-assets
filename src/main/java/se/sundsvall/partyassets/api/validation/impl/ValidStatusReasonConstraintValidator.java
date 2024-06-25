package se.sundsvall.partyassets.api.validation.impl;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.allNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper;
import org.springframework.web.context.request.RequestContextHolder;

import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.service.StatusService;

abstract class ValidStatusReasonConstraintValidator {

	private static final String ERROR_MESSAGE_TEMPLATE = "'%s' is not valid reason for status %s. Valid reasons are %s.";

	private static final String MUNICIPALITY_ID = "municipalityId";

	private static final String DEFAULT_MUNICIPALITY_ID = "2281";

	private final StatusService statusService;

	protected ValidStatusReasonConstraintValidator(final StatusService statusService) {
		this.statusService = statusService;
	}

	boolean noStatusReason(final Status status, final String statusReason) {
		if (allNull(status, statusReason)) {
			return true;
		}
		final var municipalityId = getMunicipalityIdFromPath();

		return !statusService.getReasonsForAllStatuses(municipalityId).containsKey(status) && isNull(statusReason);
	}

	/**
	 * TODO: Remove this and use the DEPT-44 version instead when that is implemented (UF-8954)
	 * Getting value for path variable name from current request
	 *
	 * @return value of path parameter that matches sent in variable name
	 */
	String getMunicipalityIdFromPath() {
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
			.orElse(DEFAULT_MUNICIPALITY_ID);
	}

	boolean isValidStatusReason(final Status status, final String statusReason) {
		if (allNull(status, statusReason)) {
			return true;
		}
		final var municipalityId = getMunicipalityIdFromPath();

		return isNotEmpty(statusReason) &&
			statusService.getReasonsForAllStatuses(municipalityId).getOrDefault(status, emptyList()).contains(statusReason);
	}

	void useCustomMessageForValidation(final ConstraintValidatorContext constraintContext, final Status status, final String statusReason) {

		final var municipalityId = getMunicipalityIdFromPath();

		constraintContext.disableDefaultConstraintViolation();
		constraintContext.buildConstraintViolationWithTemplate(InterpolationHelper.escapeMessageParameter(
				String.format(ERROR_MESSAGE_TEMPLATE,
					statusReason,
					status,
					statusService.getReasonsForAllStatuses(municipalityId).getOrDefault(status, emptyList()))))
			.addConstraintViolation();
	}

}
