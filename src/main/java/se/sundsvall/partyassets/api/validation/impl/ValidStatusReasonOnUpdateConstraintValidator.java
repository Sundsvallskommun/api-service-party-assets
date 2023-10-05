package se.sundsvall.partyassets.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.partyassets.api.model.AssetUpdateRequest;
import se.sundsvall.partyassets.api.validation.ValidStatusReason;

public class ValidStatusReasonOnUpdateConstraintValidator extends ValidStatusReasonConstraintValidator implements ConstraintValidator<ValidStatusReason, AssetUpdateRequest> {
	@Override
	public boolean isValid(final AssetUpdateRequest request, final ConstraintValidatorContext context) {
		final boolean isValid = noStatusReason(request.getStatus(), request.getStatusReason()) || isValidStatusReason(request.getStatus(), request.getStatusReason());

		if (!isValid) {
			useCustomMessageForValidation(context, request.getStatus(), request.getStatusReason());
		}

		return isValid;
	}
}
