package se.sundsvall.partyassets.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.dept44.models.api.paging.AbstractParameterPagingBase;
import se.sundsvall.partyassets.api.validation.ValidPageOffset;

public class ValidPageOffsetConstraintValidator implements ConstraintValidator<ValidPageOffset, AbstractParameterPagingBase> {

	private static final String MESSAGE_TEMPLATE = "must be at most %d when limit is %d";

	@Override
	public boolean isValid(final AbstractParameterPagingBase parameters, final ConstraintValidatorContext context) {
		if (parameters == null || parameters.getPage() < 1 || parameters.getLimit() < 1) {
			return true;
		}

		// JPA takes the number of results to skip, (page - 1) * limit, as an int
		final var lastPage = Integer.MAX_VALUE / parameters.getLimit() + 1L;
		if (parameters.getPage() <= lastPage) {
			return true;
		}

		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(MESSAGE_TEMPLATE.formatted(lastPage, parameters.getLimit()))
			.addPropertyNode("page")
			.addConstraintViolation();
		return false;
	}
}
