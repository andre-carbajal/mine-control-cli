package net.andrecarbajal.mine_control_cli.validator;

import net.andrecarbajal.mine_control_cli.validator.core.IValidator;
import net.andrecarbajal.mine_control_cli.validator.core.ValidationResult;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ServerFileValidator implements IValidator<String> {
    @Override
    public ValidationResult validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.invalid("Server file path cannot be null or empty");
        }

        Path path = Path.of(value);
        if (!Files.exists(path)) {
            return ValidationResult.invalid("Server file does not exist: " + value);
        }

        return ValidationResult.valid();
    }
}