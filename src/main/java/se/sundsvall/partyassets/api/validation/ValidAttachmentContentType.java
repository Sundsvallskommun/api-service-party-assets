package se.sundsvall.partyassets.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.partyassets.api.validation.impl.ValidAttachmentContentTypeConstraintValidator;

@Documented
@Target({
	ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidAttachmentContentTypeConstraintValidator.class)
public @interface ValidAttachmentContentType {

	String message() default "content type is not allowed for attachments";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
