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
            if (!configurationManager.getBoolean("potato-peeler.enabled")) return;
            File potatoPeelerPath = new File(pathsConfiguration.getBaseDir().toFile(), "potatopeeler");
            Path jarPathToUse = null;
            if (Files.exists(potatoPeelerPath.toPath())) {
                try (var stream = Files.list(potatoPeelerPath.toPath())) {
                    Path jarPath = stream
                            .filter(p -> p.getFileName().toString().matches("PotatoPeeler-.*-java\\d+\\.jar"))
                            .findFirst()
                            .orElse(null);
                    if (jarPath == null) {
                        if (githubService.getLatestReleaseAssetList().isPresent()) {
                            downloadService.downloadFile(githubService.getLatestReleaseAssetList().get().getFirst().getBrowserDownloadUrl(), potatoPeelerPath.toPath());
                            try (var newStream = Files.list(potatoPeelerPath.toPath())) {
                                jarPathToUse = newStream
                                        .filter(p -> p.getFileName().toString().matches("PotatoPeeler-.*-java\\d+\\.jar"))
                                        .findFirst()
                                        .orElse(null);
                            }
                        }
                    } else {
                        String fileName = jarPath.getFileName().toString();
                        String version = fileName.replaceAll("PotatoPeeler-(.*)-java\\d+\\.jar", "$1");
                        boolean isLatest = githubService.isCurrentVersionLatest(version);
                        if (!isLatest) {
                            if (githubService.getLatestReleaseAssetList().isPresent()) {
                                Files.delete(jarPath);
                                downloadService.downloadFile(githubService.getLatestReleaseAssetList().get().getFirst().getBrowserDownloadUrl(), potatoPeelerPath.toPath());
                                try (var newStream = Files.list(potatoPeelerPath.toPath())) {
                                    jarPathToUse = newStream
                                            .filter(p -> p.getFileName().toString().matches("PotatoPeeler-.*-java\\d+\\.jar"))
                                            .findFirst()
                                            .orElse(null);
                                }
                            }
                        } else {
                            jarPathToUse = jarPath;
                        }
                    }
                }
            } else {
                FileUtil.createDirectoryIfNotExists(potatoPeelerPath.toPath());
                if (githubService.getLatestReleaseAssetList().isPresent()) {
                    downloadService.downloadFile(githubService.getLatestReleaseAssetList().get().getFirst().getBrowserDownloadUrl(), potatoPeelerPath.toPath());
                    try (var newStream = Files.list(potatoPeelerPath.toPath())) {
                        jarPathToUse = newStream
                                .filter(p -> p.getFileName().toString().matches("PotatoPeeler-.*-java\\d+\\.jar"))
                                .findFirst()
                                .orElse(null);
                    }
                }
            }

            if (jarPathToUse != null) {
                executionService.startPotatoPeeler(new File(configurationManager.getString("paths.servers"), serverName), jarPathToUse, loaderType);
            }
        }catch (IOException ioException) {
            System.out.println(TextDecorationUtil.error("Error while trying to remove unused chunks: " + ioException.getMessage()));
        } catch (Exception e) {
            System.out.println(TextDecorationUtil.error("An unexpected error occurred while trying to remove unused chunks: " + e.getMessage()));
        }
    }
}
