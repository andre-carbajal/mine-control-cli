package net.andrecarbajal.mine_control_cli.validator;

import org.springframework.stereotype.Component;

@Component
public class FolderNameValidator implements IValidator {
    @Override
    public boolean isValid(String folderName) {
        if (folderName == null || folderName.trim().isEmpty()) {
            return false;
        }

        // Check length (Windows has a 255-character limit, which is a good general limit)
        if (folderName.length() > 255) {
            return false;
        }

        // Check for reserved names in Windows (regardless of case)
        String[] reservedNames = {
                "CON", "PRN", "AUX", "NUL",
                "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
                "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
        };

        String upperFolderName = folderName.toUpperCase();
        for (String reserved : reservedNames) {
            if (upperFolderName.equals(reserved) ||
                    upperFolderName.startsWith(reserved + ".")) {
                return false;
            }
        }

        // Check for invalid characters across all operating systems
        // This includes characters invalid in Windows, Linux, and macOS
        String invalidChars = "[\\\\/:*?\"<>|\\x00-\\x1F]";
        if (folderName.matches(".*" + invalidChars + ".*")) {
            return false;
        }

        // Check for leading or trailing spaces/dots
        if (folderName.startsWith(" ") || folderName.endsWith(" ") ||
                folderName.startsWith(".") || folderName.endsWith(".")) {
            return false;
        }

        // Check for consecutive dots (could be interpreted as parent directory)
        if (folderName.contains("..")) {
            return false;
        }

        return true;
    }
}
