package net.andrecarbajal.mine_control_cli.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.andrecarbajal.mine_control_cli.model.github.GithubTagResponse;
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
import java.util.List;
import java.util.Properties;

@Order(2)
@Configuration
@Getter
@PropertySource(value = "file:${config.path}", ignoreResourceNotFound = true)
public class MineControlConfig {
    private final AppProperties appProperties;
    private final ApplicationPath applicationPath;
    private Properties configProperties;
    private Path instancesPath;
    private Path backupsPath;

    public MineControlConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.applicationPath = new ApplicationPath(appProperties);
    }

    @PostConstruct
    public void initialize() {
        applicationPath.createApplicationPath();
        initializeConfigProperties();
        initializePaths();

        var configPath = applicationPath.getApplicationPath().resolve("config.properties");
        System.setProperty("config.path", configPath.toString());
    }

    private void initializeConfigProperties() {
        DefaultConfig defaultConfig = new DefaultConfig(applicationPath);
        try {
            configProperties = defaultConfig.loadConfig();
        } catch (Exception e) {
            throw new RuntimeException("Error loading configuration", e);
        }
    }

    private void initializePaths() {
        instancesPath = configProperties.getProperty("cli.instances") != null ?
                Path.of(configProperties.getProperty("cli.instances")) :
                applicationPath.getApplicationPath().resolve("instances");
        backupsPath = configProperties.getProperty("cli.backups") != null ?
                Path.of(configProperties.getProperty("cli.backups")) :
                applicationPath.getApplicationPath().resolve("backups");
        try {
            Files.createDirectories(instancesPath);
            Files.createDirectories(backupsPath);
        } catch (Exception e) {
            throw new RuntimeException("Error creating application directories", e);
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