package net.andrecarbajal.mine_control_cli.config;

import lombok.AllArgsConstructor;
import net.andrecarbajal.mine_control_cli.validator.ConfigValidator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@AllArgsConstructor
public class DefaultConfig {
    private final MineControlConfig mineControlConfig;
    private final ConfigValidator validator = new ConfigValidator();

    private static final Set<String> REQUIRED_PROPERTIES = new HashSet<>(
            Arrays.asList("server.ram", "java.path", "cli.instances", "cli.backups")
    );

    public Properties loadConfig() throws IOException {
        Path configPath = mineControlConfig.getApplicationFolder().resolve("config.properties");
        Properties properties = new Properties();

        if (configPath.toFile().exists()) {
            try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
                properties.load(fis);

                boolean missingProperties = checkMissingProperties(properties);

                try {
                    validator.validateConfig(properties);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    missingProperties = true;
                }

                if (missingProperties) {
                    System.out.println("Missing or invalid properties. Resetting to defaults.");
                    properties.clear();
                    setDefaultProperties(properties);
                    try (FileOutputStream fos = new FileOutputStream(configPath.toFile())) {
                        properties.store(fos, "Reset to default after validation failure or missing properties");
                    }
                }
            }
        } else {
            setDefaultProperties(properties);
            try (FileOutputStream fos = new FileOutputStream(configPath.toFile())) {
                properties.store(fos, "Default config");
            }
            System.out.println("Config file created at: " + configPath);
        }
        return properties;
    }

    private boolean checkMissingProperties(Properties properties) {
        Set<String> missingProps = new HashSet<>();

        for (String requiredProp : REQUIRED_PROPERTIES) {
            if (!properties.containsKey(requiredProp)) {
                missingProps.add(requiredProp);
            }
        }

        if (!missingProps.isEmpty()) {
            System.out.println("Missing required properties: " + String.join(", ", missingProps));
            return true;
        }

        return false;
    }

    private void setDefaultProperties(Properties properties) {
        properties.setProperty("server.ram", "2G");
        properties.setProperty("java.path", "java");
        properties.setProperty("cli.instances", mineControlConfig.getInstancesPath().toString());
        properties.setProperty("cli.backups", mineControlConfig.getBackupsPath().toString());
    }
}