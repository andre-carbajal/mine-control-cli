package net.andrecarbajal.mine_control_cli.validator.core;

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

    public boolean isNotValid() {
        return !errors.isEmpty();
    }
}