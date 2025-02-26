package net.andrecarbajal.mine_control_cli.config.properties;

import lombok.AllArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.path.ApplicationPathResolver;
import net.andrecarbajal.mine_control_cli.validator.ConfigValidator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

@AllArgsConstructor
public class ConfigurationManager {
    private final ApplicationPathResolver pathResolver;
    private final ConfigValidator validator = new ConfigValidator();

    public ConfigProperties configProperties() {
        Path configFilePath = pathResolver.getApplicationPath().resolve("config.properties");
        ConfigProperties configProperties = new ConfigProperties();

        try {
            if (configFilePath.toFile().exists()) {
                loadConfiguration(configFilePath, configProperties);
            } else {
                configProperties.setInstancesPath(pathResolver.createSubdirectory("instances"));
                configProperties.setBackupsPath(pathResolver.createSubdirectory("backups"));

                saveConfiguration(configFilePath, configProperties);
                System.out.println("Config file created at: " + configFilePath);
            }

            System.setProperty("config.path", configFilePath.toString());

            return configProperties;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize configuration", e);
        }
    }

    private void loadConfiguration(Path configPath, ConfigProperties configProperties) throws IOException {
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
            properties.load(fis);

            if (properties.containsKey("server.ram")) {
                configProperties.setServerRam(properties.getProperty("server.ram"));
            }

            if (properties.containsKey("java.path")) {
                configProperties.setJavaPath(properties.getProperty("java.path"));
            }

            if (properties.containsKey("cli.instances")) {
                configProperties.setInstancesPath(Path.of(properties.getProperty("cli.instances")));
            } else {
                configProperties.setInstancesPath(pathResolver.createSubdirectory("instances"));
            }

            if (properties.containsKey("cli.backups")) {
                configProperties.setBackupsPath(Path.of(properties.getProperty("cli.backups")));
            } else {
                configProperties.setBackupsPath(pathResolver.createSubdirectory("backups"));
            }

            List<String> validationErrors = validator.validate(configProperties);
            if (!validationErrors.isEmpty()) {
                System.out.println("Configuration validation failed: " + validationErrors);
                resetToDefaults(configPath, configProperties);
            }
        }

        pathResolver.createSubdirectory(configProperties.getInstancesPath().getFileName().toString());
        pathResolver.createSubdirectory(configProperties.getBackupsPath().getFileName().toString());
    }

    private void resetToDefaults(Path configPath, ConfigProperties configProperties) throws IOException {
        System.out.println("Resetting configuration to defaults due to validation failures");

        configProperties.setServerRam("2G");
        configProperties.setJavaPath("java");
        configProperties.setInstancesPath(pathResolver.createSubdirectory("instances"));
        configProperties.setBackupsPath(pathResolver.createSubdirectory("backups"));

        saveConfiguration(configPath, configProperties);
    }

    private void saveConfiguration(Path configPath, ConfigProperties configProperties) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("server.ram", configProperties.getServerRam());
        properties.setProperty("java.path", configProperties.getJavaPath());
        properties.setProperty("cli.instances", configProperties.getInstancesPath().toString());
        properties.setProperty("cli.backups", configProperties.getBackupsPath().toString());

        try (FileOutputStream fos = new FileOutputStream(configPath.toFile())) {
            properties.store(fos, "MineControl CLI Configuration");
        }
    }
}