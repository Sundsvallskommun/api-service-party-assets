package se.sundsvall.partyassets.api.validation.impl;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.allNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.service.StatusService;

abstract class ValidStatusReasonConstraintValidator {
	private static final String ERROR_MESSAGE_TEMPLATE = "'%s' is not valid reason for status %s. Valid reasons are %s.";

	@Autowired
	private StatusService statusService;

	boolean noStatusReason(final Status status, final String statusReason) {
		if (allNull(status, statusReason)) {
			return true;
		}

		return !statusService.getReasonsForAllStatuses().containsKey(status) && isNull(statusReason);
	}

	boolean isValidStatusReason(final Status status, final String statusReason) {
		if (allNull(status, statusReason)) {
			return true;
		}

		return isNotEmpty(statusReason) &&
			statusService.getReasonsForAllStatuses().getOrDefault(status, emptyList()).contains(statusReason);
	}

	void useCustomMessageForValidation(final ConstraintValidatorContext constraintContext, final Status status, final String statusReason) {
		constraintContext.disableDefaultConstraintViolation();
		constraintContext.buildConstraintViolationWithTemplate(InterpolationHelper.escapeMessageParameter(
			String.format(ERROR_MESSAGE_TEMPLATE,
				statusReason,
				status,
				statusService.getReasonsForAllStatuses().getOrDefault(status, emptyList()))))
			.addConstraintViolation();
	}
}
