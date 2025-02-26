package net.andrecarbajal.mine_control_cli.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.andrecarbajal.mine_control_cli.config.path.ApplicationPathResolver;
import net.andrecarbajal.mine_control_cli.config.properties.ConfigProperties;
import net.andrecarbajal.mine_control_cli.config.properties.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.util.ProgressBar;
import net.andrecarbajal.mine_control_cli.util.UpdateChecker;
import org.jline.terminal.Terminal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@Configuration
@PropertySource(value = "file:${config.path}", ignoreResourceNotFound = true)
public class AppConfiguration {
    private final ApplicationProperties applicationProperties;
    private final ApplicationPathResolver applicationPathResolver;
    private final UpdateChecker updateChecker;
    private ConfigProperties configProperties;
    private Path instancesPath;
    private Path backupsPath;

    public AppConfiguration(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.applicationPathResolver = new ApplicationPathResolver(applicationProperties);
        this.updateChecker = new UpdateChecker(applicationProperties);
    }

    @PostConstruct
    public void initialize() {
        applicationPathResolver.createApplicationPath();
        initializeConfigProperties();
        initializePaths();
    }

    private void initializeConfigProperties() {
        ConfigurationManager configurationManager = new ConfigurationManager(applicationPathResolver);
        try {
            configProperties = configurationManager.configProperties();
        } catch (Exception e) {
            throw new RuntimeException("Error loading configuration", e);
        }
    }

    private void initializePaths() {
        instancesPath = configProperties.getInstancesPath();
        backupsPath = configProperties.getBackupsPath();

        try {
            Files.createDirectories(instancesPath);
            Files.createDirectories(backupsPath);
        } catch (Exception e) {
            throw new RuntimeException("Error creating application directories", e);
        }
    }

    @Bean
    public String updateChecker(){
        updateChecker.checkForUpdates().ifPresent(update -> {
            System.out.println("A new version is available: " + update.version());
            System.out.println("Download it at: " + update.downloadUrl());
        });
        return "Update Checker";
    }

    @Bean
    public ConfigProperties configProperties() {
        return configProperties;
    }

    @Bean
    public ProgressBar progressBar(Terminal terminal) {
        return new ProgressBar(terminal);
    }
}