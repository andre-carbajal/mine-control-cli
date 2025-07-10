package net.andrecarbajal.mine_control_cli.validator.config;

import net.andrecarbajal.mine_control_cli.validator.core.IValidator;
import net.andrecarbajal.mine_control_cli.validator.core.ValidationResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JavaPathValidator implements IValidator<String> {
    @Override
    public ValidationResult validate(String javaPath) {
        if (javaPath == null) {
            return ValidationResult.invalid("Java path cannot be null");
        }

        if (javaPath.equals("java")) {
            String pathEnv = System.getenv("PATH");
            if (pathEnv == null) {
                return ValidationResult.invalid("PATH environment variable not set");
            }

            for (String path : pathEnv.split(File.pathSeparator)) {
                Path javaBin = Paths.get(path, "java" + (System.getProperty("os.name").toLowerCase().contains("win") ? ".exe" : ""));
                if (Files.isExecutable(javaBin)) {
                    return ValidationResult.valid();
                }
            }
            return ValidationResult.invalid("Java executable not found in PATH");
        }

        Path javaExecutable = Paths.get(javaPath);
        if (!Files.isExecutable(javaExecutable)) {
            return ValidationResult.invalid("Java executable not found or not executable at: " + javaPath);
        }

        return ValidationResult.valid();
    }
}
