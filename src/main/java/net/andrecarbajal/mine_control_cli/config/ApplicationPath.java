package net.andrecarbajal.mine_control_cli.config;

import net.andrecarbajal.mine_control_cli.util.OsChecker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplicationPath {
    private final AppProperties appProperties;

    public ApplicationPath(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public Path getApplicationPath() {
        String baseFolder = switch (OsChecker.getOperatingSystemType()) {
            case Windows -> System.getenv("APPDATA");
            case MacOS -> System.getProperty("user.home") + "/Library/Application Support";
            default -> System.getProperty("user.home");
        };

        return Paths.get(baseFolder, appProperties.getName());
    }

    public void createApplicationPath() {
        Path applicationPath = getApplicationPath();
        try {
            Files.createDirectories(applicationPath);
        } catch (Exception e) {
            throw new RuntimeException("Error creating application folder", e);
        }
    }
}
