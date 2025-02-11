package net.andrecarbajal.mine_control_cli.util;

import net.andrecarbajal.mine_control_cli.Application;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

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

    public static void deleteFolder(Path folderPath) {
        try {
            if (!Files.exists(folderPath)) {
                System.out.println("La carpeta no existe: " + folderPath);
                return;
            }

            if (!Files.isDirectory(folderPath)) {
                System.out.println("La ruta no es un directorio: " + folderPath);
                return;
            }

            Files.walkFileTree(folderPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!dir.equals(folderPath)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e) {
            System.out.println("Error deleting folder: " + e.getMessage());
        }
    }

    public static void saveEulaFile(Path serverPath) {
        Path eulaPath = serverPath.resolve("eula.txt");
        String content = "eula=true";

        try {
            Files.writeString(eulaPath, content);
        } catch (IOException e) {
            throw new RuntimeException("Error creating eula file", e);
        }
    }
}
