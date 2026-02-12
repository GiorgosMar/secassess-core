package org.secassess.core.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom annotation to validate that a string follows the Semantic Versioning (SemVer) format.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SemVerValidator.class)
@Documented
public @interface SemVer {
    String message() default "Invalid Semantic Version format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}