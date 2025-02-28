package net.andrecarbajal.mine_control_cli.config.path;

import net.andrecarbajal.mine_control_cli.config.ApplicationProperties;
import net.andrecarbajal.mine_control_cli.util.system.OsChecker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplicationPathResolver {
    private final ApplicationProperties applicationProperties;

    public ApplicationPathResolver(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public Path getApplicationPath() {
        String baseFolder = switch (OsChecker.getOperatingSystemType()) {
            case Windows -> System.getenv("APPDATA");
            case MacOS -> System.getProperty("user.home") + "/Library/Application Support";
            default -> System.getProperty("user.home");
        };

        return Paths.get(baseFolder, applicationProperties.getName());
    }

    public void createApplicationPath() {
        Path applicationPath = getApplicationPath();
        try {
            Files.createDirectories(applicationPath);
        } catch (Exception e) {
            throw new RuntimeException("Error creating application folder", e);
        }
    }

    public Path createSubdirectory(String subdirectory) {
        Path subPath = getApplicationPath().resolve(subdirectory);
        try {
            Files.createDirectories(subPath);
            return subPath;
        } catch (Exception e) {
            throw new RuntimeException("Error creating " + subdirectory + " directory", e);
        }
    }
}
