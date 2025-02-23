package net.andrecarbajal.mine_control_cli.util;

import lombok.AllArgsConstructor;
import net.andrecarbajal.mine_control_cli.Application;
import net.andrecarbajal.mine_control_cli.config.AppProperties;
import net.andrecarbajal.mine_control_cli.model.ServerLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class FileUtil {
    private final AppProperties appProperties;

    public Path getMineControlCliFolder() {
        String folder = switch (OsChecker.getOperatingSystemType()) {
            case Windows -> System.getenv("APPDATA");
            case MacOS -> System.getProperty("user.home") + "/Library/Application Support";
            default -> System.getProperty("user.home");
        };

        return Paths.get(folder, appProperties.getName());
    }

    public Path getConfiguration() {
        return getMineControlCliFolder().resolve("config.properties");
    }

    public Path getServerInstancesFolder() {
        return getMineControlCliFolder().resolve("instances");
    }

    public Path getServerBackupsFolder() {
        Path backupsFolder = getMineControlCliFolder().resolve("backups");
        if (!Files.exists(backupsFolder))
            createFolder(backupsFolder);
        return backupsFolder;
    }

    public void createFolder(Path path) {
        if (Files.exists(path)) {
            throw new RuntimeException("Folder already exists");
        }
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("Error creating folder", e);
        }
    }

    public void deleteFolder(Path folderPath) {
        try {
            if (!Files.exists(folderPath)) {
                throw new RuntimeException("Folder does not exist: " + folderPath);
            }

            if (!Files.isDirectory(folderPath)) {
                throw new RuntimeException("Path is not a folder: " + folderPath);
            }

            try (Stream<Path> stream = Files.list(folderPath)) {
                if (stream.findAny().isEmpty()) {
                    Files.delete(folderPath);
                    return;
                }
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

            Files.delete(folderPath);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting folder: " + folderPath, e);
        }
    }

    public void saveEulaFile(Path serverPath) {
        Path eulaPath = serverPath.resolve("eula.txt");
        String content = "eula=true";

        try {
            Files.writeString(eulaPath, content);
        } catch (IOException e) {
            throw new RuntimeException("Error creating eula file", e);
        }
    }

    public void saveServerInfo(Path serverPath, ServerLoader serverLoader, String version) {
        Path infoPath = serverPath.resolve("mineControlServer.info");
        String content = String.format("serverLoader: %s\nversion: %s", serverLoader, version);

        try {
            Files.writeString(infoPath, content);
        } catch (IOException e) {
            throw new RuntimeException("Error creating server info file", e);
        }
    }

    public void saveServerInfo(Path serverPath, ServerLoader serverLoader, String version, String loader) {
        Path infoPath = serverPath.resolve("mineControlServer.info");
        String content = String.format("serverLoader: %s\nversion: %s\nloader: %s", serverLoader, version, loader);

        try {
            Files.writeString(infoPath, content);
        } catch (IOException e) {
            throw new RuntimeException("Error creating server info file", e);
        }
    }

    public List<String[]> getFilesInFolderWithDetails(Path folderPath) {
        try (Stream<Path> paths = Files.list(folderPath)) {
            return paths.map(path -> {
                String folderName = path.getFileName().toString();
                Path infoPath = path.resolve("mineControlServer.info");
                if (Files.exists(infoPath)) {
                    try {
                        List<String> lines = Files.readAllLines(infoPath);
                        String serverLoader = lines.stream()
                                .filter(line -> line.startsWith("serverLoader:"))
                                .map(line -> line.split(": ", 2))
                                .filter(parts -> parts.length == 2)
                                .map(parts -> parts[1])
                                .findFirst()
                                .orElse("Unknown");

                        String version = lines.stream()
                                .filter(line -> line.startsWith("version:"))
                                .map(line -> line.split(": ", 2))
                                .filter(parts -> parts.length == 2)
                                .map(parts -> parts[1])
                                .findFirst()
                                .orElse("Unknown");

                        String loader = lines.stream()
                                .filter(line -> line.startsWith("loader:"))
                                .map(line -> line.split(": ", 2))
                                .filter(parts -> parts.length == 2)
                                .map(parts -> parts[1])
                                .findFirst()
                                .orElse(null);

                        if (loader != null)
                            return new String[]{folderName, String.format("%s (%s-%s-%s)", folderName, serverLoader, version, loader)};
                        else
                            return new String[]{folderName, String.format("%s (%s-%s)", folderName, serverLoader, version)};
                    } catch (IOException e) {
                        return new String[]{folderName, String.format("%s (Unknown)", folderName)};
                    }
                } else {
                    return new String[]{folderName, String.format("%s (Unknown)", folderName)};
                }
            }).toList();
        } catch (IOException e) {
            throw new RuntimeException("Error getting files in folder", e);
        }
    }
}
