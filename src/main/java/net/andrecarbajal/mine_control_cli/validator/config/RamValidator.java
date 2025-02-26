package net.andrecarbajal.mine_control_cli.validator.config;

import net.andrecarbajal.mine_control_cli.validator.core.IValidator;
import net.andrecarbajal.mine_control_cli.validator.core.ValidationResult;

public class RamValidator implements IValidator<String> {
    @Override
    public ValidationResult validate(String ram) {
        if (ram == null || !ram.matches("\\d+[GM]")) {
            return ValidationResult.invalid("Invalid RAM format. Expected format: NUMBER[G|M], e.g. 2G or 1024M");
        }
        return ValidationResult.valid();
    }
}
