package net.andrecarbajal.mine_control_cli.validator;

public abstract class BasePropertyValidator implements IPropertyValidator {
    private final String propertyName;
    private final String description;

    protected BasePropertyValidator(String propertyName, String description) {
        this.propertyName = propertyName;
        this.description = description;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    protected void checkEmpty(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Value cannot be empty");
        }
    }
}
