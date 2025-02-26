package net.andrecarbajal.mine_control_cli.validator.core;

public interface IValidator<T> {
    ValidationResult validate(T value);

    default boolean isValid(T value) {
        return validate(value).isValid();
    }
}
