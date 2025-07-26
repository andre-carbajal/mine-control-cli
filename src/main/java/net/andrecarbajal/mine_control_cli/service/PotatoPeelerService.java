package net.andrecarbajal.mine_control_cli.service;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.config.PathsConfiguration;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class PotatoPeelerService {
    private final ConfigurationManager configurationManager;
    private final PathsConfiguration pathsConfiguration;
    private final GithubService githubService = new GithubService("Bottle-M", "PotatoPeeler");
    private final DownloadService downloadService;
    private final ExecutionService executionService;

    public void removeUnusedChunks(String serverName, String loaderType) {
        try {
            File potatoPeelerPath = getPotatoPeelerPath();
            Path jarPathToUse;
            if (Files.exists(potatoPeelerPath.toPath())) {
                Path jarPath = findLocalJar(potatoPeelerPath);
                if (jarPath == null) {
                    jarPathToUse = downloadAndSelectJar(potatoPeelerPath);
                } else if (!isLatestVersion(jarPath)) {
                    Files.delete(jarPath);
                    jarPathToUse = downloadAndSelectJar(potatoPeelerPath);
                } else {
                    jarPathToUse = jarPath;
                }
            } else {
                ensureDirectoryExists(potatoPeelerPath);
                jarPathToUse = downloadAndSelectJar(potatoPeelerPath);
            }
            if (jarPathToUse != null) {
                executePotatoPeeler(serverName, jarPathToUse, loaderType);
                System.out.println(TextDecorationUtil.success("Unused chunks removal process started for server '" + serverName + "'."));
            }
        } catch (IOException ioException) {
            System.out.println(TextDecorationUtil.error("Error while trying to remove unused chunks: " + ioException.getMessage()));
        } catch (Exception e) {
            System.out.println(TextDecorationUtil.error("An unexpected error occurred while trying to remove unused chunks: " + e.getMessage()));
        }
    }

    private File getPotatoPeelerPath() {
        return new File(pathsConfiguration.getBaseDir().toFile(), "potatopeeler");
    }

    private Path findLocalJar(File potatoPeelerPath) throws IOException {
        try (var stream = Files.list(potatoPeelerPath.toPath())) {
            return stream
                .filter(p -> p.getFileName().toString().matches("PotatoPeeler-.*-java\\d+\\.jar"))
                .findFirst()
                .orElse(null);
        }
    }

    private boolean isLatestVersion(Path jarPath) {
        String fileName = jarPath.getFileName().toString();
        String version = fileName.replaceAll("PotatoPeeler-(.*)-java\\d+\\.jar", "$1");
        return githubService.isCurrentVersionLatest(version);
    }

    private Path downloadAndSelectJar(File potatoPeelerPath) throws IOException {
        if (githubService.getLatestReleaseAssetList().isPresent()) {
            downloadService.downloadFile(
                githubService.getLatestReleaseAssetList().get().getFirst().browserDownloadUrl(),
                potatoPeelerPath.toPath()
            );
            return findLocalJar(potatoPeelerPath);
        }
        return null;
    }

    private void ensureDirectoryExists(File potatoPeelerPath) throws IOException {
        FileUtil.createDirectoryIfNotExists(potatoPeelerPath.toPath());
    }

    private void executePotatoPeeler(String serverName, Path jarPathToUse, String loaderType) {
        executionService.startPotatoPeeler(
            new File(configurationManager.getString("paths.servers"), serverName),
            jarPathToUse,
            loaderType
        );
    }
}
