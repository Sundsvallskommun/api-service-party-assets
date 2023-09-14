package se.sundsvall.citizenassets.api.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.citizenassets.api.model.AssetCreateRequest;
import se.sundsvall.citizenassets.api.validation.ValidStatusReason;

public class ValidStatusReasonOnCreateConstraintValidator extends ValidStatusReasonConstraintValidator implements ConstraintValidator<ValidStatusReason, AssetCreateRequest> {
	@Override
	public boolean isValid(final AssetCreateRequest request, final ConstraintValidatorContext context) {
		final boolean isValid = noStatusReason(request.getStatus(), request.getStatusReason()) || isValidStatusReason(request.getStatus(), request.getStatusReason());

		if (!isValid) {
			useCustomMessageForValidation(context, request.getStatus(), request.getStatusReason());
		}

		return isValid;
	}
}
