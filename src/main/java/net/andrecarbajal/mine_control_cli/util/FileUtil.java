package net.andrecarbajal.mine_control_cli.util;

import net.andrecarbajal.mine_control_cli.Application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    public static Path getMineControlCliFolder() {
        String folder = switch (OsChecker.getOperatingSystemType()) {
            case Windows -> System.getenv("APPDATA");
            case MacOS -> System.getProperty("user.home") + "/Library/Application Support";
            default -> System.getProperty("user.home");
        };

        return Paths.get(folder, Application.APP_FOLDER_NAME);
    }

    public static void createFolder(Path path) {
        if (Files.exists(path)) {
            throw new RuntimeException("Folder already exists");
        }
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("Error creating folder", e);
        }
    }

    public static void saveEulaFile(Path serverPath) {
        Path eulaPath = serverPath.resolve("eula.txt");
        String content = "eula=true";

        try {
            Files.writeString(eulaPath, content);
            System.out.println("EULA file created");
        } catch (IOException e) {
            throw new RuntimeException("Error creating eula file", e);
        }
    }
}
