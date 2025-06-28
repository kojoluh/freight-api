package com.fkluh.freight.v1.util;

import com.fkluh.freight.v1.exception.PackageValidationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ValidationUtil {
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    public static <T> void validate(T object) {
        var violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new PackageValidationException(violations.iterator().next().getMessage());
        }
    }

    public static void validateNotEmpty(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void validateDateFormat(String date, String errorMessage) {
        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
