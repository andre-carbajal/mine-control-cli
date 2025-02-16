package net.andrecarbajal.mine_control_cli.validator.core;

public interface IPropertyValidator {
    void validate(String value) throws IllegalArgumentException;
    String getPropertyName();
    String getDescription();
}
