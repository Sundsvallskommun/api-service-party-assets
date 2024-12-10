package se.sundsvall.partyassets.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.partyassets.api.validation.impl.ValidStatusReasonOnCreateConstraintValidator;
import se.sundsvall.partyassets.api.validation.impl.ValidStatusReasonOnUpdateConstraintValidator;

@Documented
@Target({
	ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {
	ValidStatusReasonOnCreateConstraintValidator.class, ValidStatusReasonOnUpdateConstraintValidator.class
})
public @interface ValidStatusReason {
	String message() default "one or more of properties in list are not present in entity.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
