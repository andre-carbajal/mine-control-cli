package net.andrecarbajal.mine_control_cli.validator.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidationResult {
    private final List<String> errors;

    private ValidationResult(List<String> errors) {
        this.errors = errors;
    }

    public static ValidationResult valid() {
        return new ValidationResult(Collections.emptyList());
    }

    public static ValidationResult invalid(String error) {
        return new ValidationResult(Collections.singletonList(error));
    }

    public static ValidationResult invalid(List<String> errors) {
        return new ValidationResult(new ArrayList<>(errors));
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public ValidationResult merge(ValidationResult other) {
        if (this.isValid() && other.isValid()) {
            return ValidationResult.valid();
        }

        List<String> allErrors = new ArrayList<>(this.errors);
        allErrors.addAll(other.errors);
        return ValidationResult.invalid(allErrors);
    }
}