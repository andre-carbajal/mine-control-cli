package net.andrecarbajal.mine_control_cli.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.andrecarbajal.mine_control_cli.model.github.GithubTagResponse;
import net.andrecarbajal.mine_control_cli.util.OsChecker;
import net.andrecarbajal.mine_control_cli.util.ProgressBar;
import org.jline.terminal.Terminal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

@Order(2)
@Configuration
@Getter
@PropertySource(value = "file:${config.path}", ignoreResourceNotFound = true)
public class MineControlConfig {
    private final AppProperties appProperties;
    private Path applicationFolder;
    private Path instancesPath;
    private Path backupsPath;
    private Properties configProperties;

    public MineControlConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void initialize() {
        initializeApplicationFolder();
        initializePaths();

        initializeConfigProperties();

        var configPath = applicationFolder.resolve("config.properties");
        System.setProperty("config.path", configPath.toString());
    }

    private void initializeApplicationFolder() {
        String baseFolder = switch (OsChecker.getOperatingSystemType()) {
            case Windows -> System.getenv("APPDATA");
            case MacOS -> System.getProperty("user.home") + "/Library/Application Support";
            default -> System.getProperty("user.home");
        };

        applicationFolder = Paths.get(baseFolder, appProperties.getName());
        try {
            Files.createDirectories(applicationFolder);
        } catch (Exception e) {
            throw new RuntimeException("Error creating application folder", e);
        }
    }

    private void initializePaths() {
        instancesPath = applicationFolder.resolve("instances");
        backupsPath = applicationFolder.resolve("backups");
        try {
            Files.createDirectories(instancesPath);
            Files.createDirectories(backupsPath);
        } catch (Exception e) {
            throw new RuntimeException("Error creating application directories", e);
        }
    }

    private void initializeConfigProperties() {
        DefaultConfig defaultConfig = new DefaultConfig(this);
        try {
            configProperties = defaultConfig.loadConfig();
        } catch (Exception e) {
            throw new RuntimeException("Error loading configuration", e);
        }
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