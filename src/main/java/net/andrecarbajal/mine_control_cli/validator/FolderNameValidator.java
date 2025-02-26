package net.andrecarbajal.mine_control_cli.validator;

import net.andrecarbajal.mine_control_cli.validator.core.IValidator;
import net.andrecarbajal.mine_control_cli.validator.core.ValidationResult;
import org.springframework.stereotype.Component;

@Component
public class FolderNameValidator implements IValidator<String> {
    private static final String[] RESERVED_NAMES = {
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    };

    @Override
    public ValidationResult validate(String folderName) {
        if (folderName == null || folderName.trim().isEmpty()) {
            return ValidationResult.invalid("Folder name cannot be null or empty");
        }

        // Check length (Windows has a 255-character limit, which is a good general limit)
        if (folderName.length() > 255) {
            return ValidationResult.invalid("Folder name exceeds 255 characters");
        }

        // Check for reserved names in Windows (regardless of case)
        String upperFolderName = folderName.toUpperCase();
        for (String reserved : RESERVED_NAMES) {
            if (upperFolderName.equals(reserved) ||
                    upperFolderName.startsWith(reserved + ".")) {
                return ValidationResult.invalid("Folder name uses reserved name: " + reserved);
            }
        }

        // Check for invalid characters across all operating systems
        // This includes characters invalid in Windows, Linux, and macOS
        String invalidChars = "[\\\\/:*?\"<>|\\x00-\\x1F]";
        if (folderName.matches(".*" + invalidChars + ".*")) {
            return ValidationResult.invalid("Folder name contains invalid characters");
        }

        // Check for leading or trailing spaces/dots
        if (folderName.startsWith(" ") || folderName.endsWith(" ") ||
                folderName.startsWith(".") || folderName.endsWith(".")) {
            return ValidationResult.invalid("Folder name cannot start or end with spaces or dots");
        }

        // Check for consecutive dots (could be interpreted as parent directory)
        if (folderName.contains("..")) {
            return ValidationResult.invalid("Folder name cannot contain consecutive dots");
        }

        return ValidationResult.valid();
    }
}