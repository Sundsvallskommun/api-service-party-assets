package se.sundsvall.partyassets.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.partyassets.api.validation.impl.ValidPageOffsetConstraintValidator;

/**
 * Validates that the requested page skips at most {@link Integer#MAX_VALUE} results, the most that a database query can
 * skip. The violation is reported on {@code page}. A page or limit below 1 is left to their own constraints.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPageOffsetConstraintValidator.class)
public @interface ValidPageOffset {

	String message() default "page starts after the last result that can be fetched";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
