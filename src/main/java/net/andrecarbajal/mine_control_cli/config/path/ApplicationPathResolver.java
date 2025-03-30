package net.andrecarbajal.mine_control_cli.config.path;

import net.andrecarbajal.mine_control_cli.config.ApplicationProperties;
import net.andrecarbajal.mine_control_cli.util.system.OsChecker;
import org.springframework.core.env.Environment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplicationPathResolver {
    private final ApplicationProperties applicationProperties;
    private final Environment environment;

    public ApplicationPathResolver(ApplicationProperties applicationProperties, Environment environment) {
        this.applicationProperties = applicationProperties;
        this.environment = environment;
    }

    public Path getApplicationPath() {
        String baseFolder = switch (OsChecker.getOperatingSystemType()) {
            case Windows -> System.getenv("APPDATA");
            case MacOS -> System.getProperty("user.home") + "/Library/Application Support";
            default -> System.getProperty("user.home");
        };

        String appName = applicationProperties.getName();

        if (environment.matchesProfiles("dev")) {
            appName += "-dev";
        }

        return Paths.get(baseFolder, appName);
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