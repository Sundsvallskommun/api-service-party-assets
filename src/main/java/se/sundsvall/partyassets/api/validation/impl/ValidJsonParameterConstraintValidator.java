package se.sundsvall.partyassets.api.validation.impl;

import static java.util.Objects.isNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Optional;
import org.springframework.util.StringUtils;
import se.sundsvall.partyassets.api.model.AssetJsonParameter;
import se.sundsvall.partyassets.api.validation.ValidJsonParameter;
import se.sundsvall.partyassets.service.JsonSchemaValidationService;

public class ValidJsonParameterConstraintValidator implements ConstraintValidator<ValidJsonParameter, AssetJsonParameter> {

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
			var validationMessages = jsonSchemaValidationService.validate(
				assetJsonParameter.getValue(),
				assetJsonParameter.getSchemaId());

			validationMessages.forEach(message -> addViolation(context, Optional.ofNullable(message.getInstanceLocation()).map(Object::toString).filter(StringUtils::hasText).map(value -> value + ": ").orElse("") + message.getMessage()));

			return validationMessages.isEmpty();
		} catch (Exception e) {
			addViolation(context, e.getMessage());
			return false;
		}
	}

	// ---- Private helpers ------------------------------------------------------

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
