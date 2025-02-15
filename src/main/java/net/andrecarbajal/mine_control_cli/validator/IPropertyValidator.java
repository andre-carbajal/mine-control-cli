package net.andrecarbajal.mine_control_cli.validator;

public interface IPropertyValidator {
    void validate(String value) throws IllegalArgumentException;
    String getPropertyName();
    String getDescription();
}
