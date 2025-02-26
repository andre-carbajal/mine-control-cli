package net.andrecarbajal.mine_control_cli.validator.config;

import net.andrecarbajal.mine_control_cli.validator.core.IValidator;
import net.andrecarbajal.mine_control_cli.validator.core.ValidationResult;

import java.nio.file.Files;
import java.nio.file.Path;

public class DirectoryValidator implements IValidator<Path> {
    @Override
    public ValidationResult validate(Path path) {
        if (path == null) {
            return ValidationResult.invalid("Directory path cannot be null");
        }

        if (!Files.isDirectory(path) && !path.toFile().mkdirs()) {
            return ValidationResult.invalid("Invalid directory path: " + path);
        }

        return ValidationResult.valid();
    }
}
