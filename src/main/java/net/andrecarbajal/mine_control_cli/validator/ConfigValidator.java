package net.andrecarbajal.mine_control_cli.validator;

import net.andrecarbajal.mine_control_cli.config.properties.ConfigProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ConfigValidator {
    public List<String> validate(ConfigProperties properties) {
        List<String> errors = new ArrayList<>();

        if (!isValidRamFormat(properties.getServerRam())) {
            errors.add("Invalid server.ram format. Expected format: NUMBER[G|M], e.g. 2G or 1024M");
        }

        if (!isValidJavaPath(properties.getJavaPath())) {
            errors.add("Invalid java.path: Java executable not found or not executable");
        }

        if (!isValidDirectory(properties.getInstancesPath())) {
            errors.add("Invalid cli.instances directory path");
        }

        if (!isValidDirectory(properties.getBackupsPath())) {
            errors.add("Invalid cli.backups directory path");
        }

        return errors;
    }

    private boolean isValidRamFormat(String ram) {
        return ram != null && ram.matches("\\d+[GM]");
    }

    private boolean isValidJavaPath(String javaPath) {
        if (javaPath == null) return false;

        if (javaPath.equals("java")) {
            String pathEnv = System.getenv("PATH");
            if (pathEnv == null) return false;

            for (String path : pathEnv.split(System.getProperty("path.separator"))) {
                Path javaBin = Paths.get(path, "java" + (System.getProperty("os.name").toLowerCase().contains("win") ? ".exe" : ""));
                if (Files.isExecutable(javaBin)) {
                    return true;
                }
            }
            return false;
        }

        Path javaExecutable = Paths.get(javaPath);
        return Files.isExecutable(javaExecutable);
    }

    private boolean isValidDirectory(Path path) {
        return path != null && (Files.isDirectory(path) || path.toFile().mkdirs());
    }
}
