package net.andrecarbajal.mine_control_cli.config;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import net.andrecarbajal.mine_control_cli.model.github.GithubTagResponse;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.ProgressBar;
import org.jline.terminal.Terminal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

@Configuration
@AllArgsConstructor
@PropertySource(value = "file:${config.path}", ignoreResourceNotFound = true)
public class MineControlConfig {
    private final FileUtil fileUtil;
    private final AppProperties appProperties;
    private final DefaultConfig defaultConfig;

    @PostConstruct
    public void setConfigPath() {
        var configPath = fileUtil.getConfiguration();
        System.setProperty("config.path", configPath.toString());
    }

    @Bean
    public Properties minecraftProperties() throws IOException {
        return defaultConfig.loadConfig();
    }

    @Bean
    public String init() {
        Path mineControlCliFolder = fileUtil.getMineControlCliFolder();
        Path serverInstancesFolder = fileUtil.getServerInstancesFolder();
        Path serverBackupsFolder = fileUtil.getServerBackupsFolder();

        if (Files.notExists(mineControlCliFolder)) {
            try {
                Files.createDirectories(mineControlCliFolder);
            } catch (Exception e) {
                throw new RuntimeException("Error creating mine-control-cli folder", e);
            }
        }

        if (Files.notExists(serverInstancesFolder)) {
            try {
                Files.createDirectories(serverInstancesFolder);
            } catch (Exception e) {
                throw new RuntimeException("Error creating server instances folder", e);
            }
        }

        if (Files.notExists(serverBackupsFolder)) {
            try {
                Files.createDirectories(serverBackupsFolder);
            } catch (Exception e) {
                throw new RuntimeException("Error creating server backups folder", e);
            }
        }

        return "mine-control-cli started";
    }

    @Bean
    public String checkUpdates() {
        RestTemplate restTemplate = new RestTemplate();

        ParameterizedTypeReference<List<GithubTagResponse>> responseType =
                new ParameterizedTypeReference<>() {
                };

        ResponseEntity<List<GithubTagResponse>> response =
                restTemplate.exchange("https://api.github.com/repos/MineControlCli/mine-control-cli/tags", HttpMethod.GET, null, responseType);

        if (response.getBody() != null) {
            var latestVersion = response.getBody().stream().map(GithubTagResponse::getName).toList().stream().findFirst().orElse(null);
            if (latestVersion != null && isNewerVersion(latestVersion)) {
                System.out.println("New version available: " + latestVersion);
                System.out.println("Download at: https://github.com/MineControlCli/mine-control-cli/releases");
            }
            return (latestVersion == null) ? " null" : latestVersion;
        } else {
            throw new RuntimeException("Error getting Fabric versions");
        }
    }

    private boolean isNewerVersion(String latestVersion) {
        latestVersion = latestVersion.startsWith("v") ? latestVersion.substring(1) : latestVersion;
        String[] latestParts = latestVersion.split("\\.");
        String[] currentParts = appProperties.getVersion().split("\\.");

        for (int i = 0; i < latestParts.length; i++) {
            int latestPart = Integer.parseInt(latestParts[i]);
            int currentPart = Integer.parseInt(currentParts[i]);

            if (latestPart > currentPart) {
                return true;
            } else if (latestPart < currentPart) {
                return false;
            }
        }
        return false;
    }

    @Bean
    public ProgressBar progressBar(Terminal terminal) {
        return new ProgressBar(terminal);
    }
}
