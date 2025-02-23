package net.andrecarbajal.mine_control_cli.validator;

import net.andrecarbajal.mine_control_cli.validator.core.IPropertyValidator;
import net.andrecarbajal.mine_control_cli.validator.property.JavaPathPropertyValidator;
import net.andrecarbajal.mine_control_cli.validator.property.RamPropertyValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigValidator {
    private final Map<String, IPropertyValidator> validators;

    public ConfigValidator() {
        validators = new HashMap<>();
        registerValidator(new RamPropertyValidator());
        registerValidator(new JavaPathPropertyValidator());
    }

    private void registerValidator(IPropertyValidator validator) {
        validators.put(validator.getPropertyName(), validator);
    }

    public void validateConfig(Properties properties) throws IllegalArgumentException {
        List<String> errors = new ArrayList<>();

        for (IPropertyValidator validator : validators.values()) {
            String propertyName = validator.getPropertyName();
            String value = properties.getProperty(propertyName);

            try {
                validator.validate(value);
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation errors found:\n" +
                    String.join("\n", errors));
        }
    }
}
