package net.andrecarbajal.mine_control_cli.validator.property;

import net.andrecarbajal.mine_control_cli.validator.core.BasePropertyValidator;

public class JavaPathPropertyValidator extends BasePropertyValidator {
    public JavaPathPropertyValidator() {
        super("java.path", "Path to the Java executable (e.g., C:\\\\Program Files\\\\Java\\\\jdk\\\\bin\\\\java.exe or /usr/bin/java)");
    }

    @Override
    public void validate(String value) throws IllegalArgumentException {
        checkEmpty(value);

        String windowsPattern = "^[A-Za-z]:[\\\\/].+";

        String unixPattern = "^/.+";

        boolean isValidFormat = value.matches(windowsPattern) || value.matches(unixPattern);

        if (!isValidFormat) {
            throw new IllegalArgumentException("Invalid path format");
        }

        String invalidChars = "<>\"|?*";
        for (char c : invalidChars.toCharArray()) {
            if (value.indexOf(c) != -1) {
                throw new IllegalArgumentException("Invalid character in path: " + c);
            }
        }

        if (value.matches(windowsPattern)) {
            int lastColonIndex = value.lastIndexOf(':');
            if (lastColonIndex != 1) {
                throw new IllegalArgumentException("Invalid colon placement in path");
            }
        } else if (value.contains(":")) {
            throw new IllegalArgumentException("Invalid character in path: :");
        }

        String[] components = value.split("[/\\\\]");
        for (String component : components) {
            if (component.isEmpty()) continue;
            if (component.equals(".") || component.equals("..")) continue;
            if (component.startsWith(" ") || component.endsWith(" ") || component.endsWith(".")) {
                throw new IllegalArgumentException("Invalid component name: " + component);
            }
        }

        String lowercasePath = value.toLowerCase();
        boolean hasValidEnding = lowercasePath.endsWith("java") ||
                lowercasePath.endsWith("java.exe") ||
                lowercasePath.endsWith("javaw") ||
                lowercasePath.endsWith("javaw.exe");

        if (!hasValidEnding) {
            throw new IllegalArgumentException("Path does not end with 'java', 'java.exe', 'javaw', or 'javaw.exe'");
        }
    }
}