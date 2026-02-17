package org.secassess.core.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Logic implementation for the @SemVer annotation that checks if a string matches the Semantic Versioning pattern.
 */
public class SemVerValidator implements ConstraintValidator<SemVer, String> {

    private static final String SEMVER_REGEX = "^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return value.matches(SEMVER_REGEX);
    }
}