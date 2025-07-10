package net.andrecarbajal.mine_control_cli.util;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;

@UtilityClass
public class SystemPathsUtil {

    public Path getSystemConfigDir(String appName) {
        OperatingSystem os = detectOperatingSystem();

        return switch (os) {
            case WINDOWS -> getWindowsConfigDir(appName);
            case MACOS -> getMacOSConfigDir(appName);
            case LINUX -> getLinuxConfigDir(appName);
            default -> getDefaultConfigDir(appName);
        };
    }

    private Path getWindowsConfigDir(String appName) {
        return Optional.ofNullable(System.getenv("APPDATA"))
                .map(appData -> Paths.get(appData, appName))
                .orElse(Paths.get(System.getProperty("user.home"), "AppData", "Roaming", appName));
    }

    private Path getMacOSConfigDir(String appName) {
        return Paths.get(System.getProperty("user.home"), "Library", "Application Support", appName);
    }

    private Path getLinuxConfigDir(String appName) {
        return Paths.get(System.getProperty("user.home"), appName);
    }

    private Path getDefaultConfigDir(String appName) {
        return Paths.get(System.getProperty("user.home"), "." + appName);
    }

    private OperatingSystem detectOperatingSystem() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

        if (osName.contains("win")) {
            return OperatingSystem.WINDOWS;
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            return OperatingSystem.MACOS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OperatingSystem.LINUX;
        } else {
            return OperatingSystem.UNKNOWN;
        }
    }

    public String getOperatingSystemName() {
        return detectOperatingSystem().getDisplayName();
    }

    @Getter
    private enum OperatingSystem {
        WINDOWS("Windows"),
        MACOS("macOS"),
        LINUX("Linux"),
        UNKNOWN("Unknown");

        private final String displayName;

        OperatingSystem(String displayName) {
            this.displayName = displayName;
        }
    }
}