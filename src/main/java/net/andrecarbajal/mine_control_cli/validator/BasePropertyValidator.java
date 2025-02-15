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
}
