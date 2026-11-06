package se.sundsvall.partyassets.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.partyassets.api.validation.impl.ValidAssetFilterConstraintValidator;

/**
 * Validates that a SpringFilter filter on assets only refers to attributes that are part of the asset API model, and
 * only consists of conditions that can be applied to those attributes, with values they can hold. A missing filter is
 * valid.
 */
@Documented
@Target({
	ElementType.PARAMETER, ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidAssetFilterConstraintValidator.class)
public @interface ValidAssetFilter {

	String message() default "filter is not a valid filter on assets";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
