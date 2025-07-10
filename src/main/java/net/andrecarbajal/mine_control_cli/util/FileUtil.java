package net.andrecarbajal.mine_control_cli.util;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@UtilityClass
public class FileUtil {
    public List<String> listDirectories(String path) {
        File dir = new File(path);
        String[] dirs = dir.list((current, name) -> new File(current, name).isDirectory());
        if (dirs == null) return List.of();
        return Arrays.asList(dirs);
    }

    public boolean directoryNotExists(String path, String dirName) {
        File dir = new File(path, dirName);
        return !dir.exists() || !dir.isDirectory();
    }

    public boolean deleteDirectoryRecursively(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteDirectoryRecursively(child)) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

    public void createDirectoryIfNotExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    public void createDirectoryIfNotExists(String pathStr) throws IOException {
        if (pathStr != null && !pathStr.isBlank()) {
            createDirectoryIfNotExists(Path.of(pathStr));
        }
    }

    public void cleanDirectory(Path dir) throws IOException {
        if (!Files.exists(dir) || !Files.isDirectory(dir)) return;
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .filter(p -> !p.equals(dir))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (Exception ignored) {
                        }
                    });
        }
    }

    public static void writeServerInfo(Path serverPath, Map<String, String> info) {
        try {
            Path infoFile = serverPath.resolve(".server-info.properties");
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : info.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
            Files.writeString(infoFile, sb.toString());
        } catch (Exception e) {
            System.out.println(TextDecorationUtil.error("Error writing server info: " + e.getMessage()));
        }
    }
}
