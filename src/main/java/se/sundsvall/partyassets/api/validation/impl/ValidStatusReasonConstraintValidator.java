package se.sundsvall.partyassets.api.validation.impl;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.allNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.sundsvall.partyassets.api.model.Status.BLOCKED;

import java.util.List;
import java.util.Map;

import org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper;

import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.partyassets.api.model.Status;

abstract class ValidStatusReasonConstraintValidator {
	private static final String ERROR_MESSAGE_TEMPLATE = "'%s' is not valid reason for status %s. Valid reasons are %s.";

	/**
	 * Map of statuses that require the status reason to be set and the values available to use for the status.
	 * If status is not present in map, status reason is validated to contain no value (i.e. is null).
	 */
	private static final Map<Status, List<String>> VALID_STATUS_REASONS_FOR_STATUSES = Map.of(
		BLOCKED, List.of("IRREGULARITY", "LOST"));

	boolean noStatusReason(final Status status, final String statusReason) {
		if (allNull(status, statusReason)) {
			return true;
		}

		return !VALID_STATUS_REASONS_FOR_STATUSES.containsKey(status) && isNull(statusReason);
	}

	boolean isValidStatusReason(final Status status, final String statusReason) {
		if (allNull(status, statusReason)) {
			return true;
		}

		return isNotEmpty(statusReason) &&
			VALID_STATUS_REASONS_FOR_STATUSES.getOrDefault(status, emptyList()).contains(statusReason);
	}

	void useCustomMessageForValidation(final ConstraintValidatorContext constraintContext, final Status status, final String statusReason) {
		constraintContext.disableDefaultConstraintViolation();
		constraintContext.buildConstraintViolationWithTemplate(InterpolationHelper.escapeMessageParameter(
			String.format(ERROR_MESSAGE_TEMPLATE,
				statusReason,
				status,
				VALID_STATUS_REASONS_FOR_STATUSES.getOrDefault(status, emptyList()))))
			.addConstraintViolation();
	}
}
