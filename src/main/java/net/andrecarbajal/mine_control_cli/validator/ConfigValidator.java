package net.andrecarbajal.mine_control_cli.validator;

import net.andrecarbajal.mine_control_cli.validator.config.DirectoryValidator;
import net.andrecarbajal.mine_control_cli.validator.config.JavaPathValidator;
import net.andrecarbajal.mine_control_cli.validator.config.RamValidator;
import net.andrecarbajal.mine_control_cli.validator.core.IValidator;
import net.andrecarbajal.mine_control_cli.validator.core.ValidationResult;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConfigValidator {
    private final Map<String, IValidator<String>> validators = new HashMap<>();

    public ConfigValidator() {
        validators.put("java.path", new JavaPathValidator());
        validators.put("java.min-ram", new RamValidator());
        validators.put("java.max-ram", new RamValidator());
        validators.put("paths.servers", path -> new DirectoryValidator().validate(Path.of(path)));
        validators.put("paths.backups", path -> new DirectoryValidator().validate(Path.of(path)));
    }

    public ValidationResult validate(String key, String value) {
        IValidator<String> validator = validators.get(key);
        if (validator != null) {
            return validator.validate(value);
        }
        return ValidationResult.valid();
    }
}