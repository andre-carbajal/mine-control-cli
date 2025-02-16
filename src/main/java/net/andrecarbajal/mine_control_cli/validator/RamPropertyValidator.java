package net.andrecarbajal.mine_control_cli.validator;

public class RamPropertyValidator extends BasePropertyValidator {
    public RamPropertyValidator() {
        super("server.ram", "Server RAM configuration (format: number followed by G or MB, e.g., 2G, 2048MB)");
    }

    @Override
    public void validate(String value) throws IllegalArgumentException {
        checkEmpty(value);

        String pattern = "^\\d+(\\.\\d+)?(G|MB)$";
        if (!value.matches(pattern)) {
            throw new IllegalArgumentException(
                    getPropertyName() + " must be a number followed by G or MB, e.g., 2G, 2048MB");
        }

        String numericPart = value.replaceAll("[GMB]", "");
        double ramSize = Double.parseDouble(numericPart);

        if (ramSize <= 0) {
            throw new IllegalArgumentException(getPropertyName() + " must be greater than 0");
        }
    }
}
