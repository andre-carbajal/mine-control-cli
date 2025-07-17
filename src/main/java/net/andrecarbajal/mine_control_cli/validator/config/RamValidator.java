package net.andrecarbajal.mine_control_cli.validator.config;

import net.andrecarbajal.mine_control_cli.validator.core.IValidator;
import net.andrecarbajal.mine_control_cli.validator.core.ValidationResult;

public class RamValidator implements IValidator<String> {
    @Override
    public ValidationResult validate(String ram) {
        if (ram == null || !ram.matches("\\d+[GMgm]")) {
            return ValidationResult.invalid("Invalid RAM format. Expected format: NUMBER[G|M|g|m], e.g. 2G, 1024M, 2g or 1024m");
        }
        return ValidationResult.valid();
    }
}
