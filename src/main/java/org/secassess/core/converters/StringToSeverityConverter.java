package org.secassess.core.converters;

import org.secassess.core.enums.Severity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Automatically converts string-based request parameters into Severity enum constants for cleaner controller methods.
 */
@Component
public class StringToSeverityConverter implements Converter<String, Severity> {
    @Override
    public Severity convert(String source) {
        try {
            return Severity.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}