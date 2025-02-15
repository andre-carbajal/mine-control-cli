package net.andrecarbajal.mine_control_cli.validator;

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
    }

    public void registerValidator(IPropertyValidator validator) {
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

    public String getValidationRules() {
        StringBuilder rules = new StringBuilder("Validation rules:\n");
        for (IPropertyValidator validator : validators.values()) {
            rules.append("- ").append(validator.getPropertyName())
                    .append(": ").append(validator.getDescription())
                    .append("\n");
        }
        return rules.toString();
    }
}
