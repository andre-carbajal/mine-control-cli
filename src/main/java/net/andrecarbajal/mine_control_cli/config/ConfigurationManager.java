package net.andrecarbajal.mine_control_cli.config;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.properties.ApplicationProperties;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import net.andrecarbajal.mine_control_cli.validator.config.DirectoryValidator;
import net.andrecarbajal.mine_control_cli.validator.config.JavaPathValidator;
import net.andrecarbajal.mine_control_cli.validator.config.RamValidator;
import net.andrecarbajal.mine_control_cli.validator.core.IValidator;
import net.andrecarbajal.mine_control_cli.validator.core.ValidationResult;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
@DependsOn("pathsConfiguration")
@RequiredArgsConstructor
public class ConfigurationManager {
    private final PathsConfiguration pathsConfiguration;
    private final ApplicationProperties applicationProperties;

    private Properties userConfig;
    private Map<String, Object> defaultConfig;

    @EventListener(ContextRefreshedEvent.class)
    public void initialize() {
        loadDefaultConfig();
        loadUserConfig();
        createCustomDirectories();
    }

    private void loadDefaultConfig() {
        defaultConfig = new HashMap<>();

        // Update configuration
        defaultConfig.put("update.check-on-startup", applicationProperties.getUpdate().isCheckOnStartup());

        // Java configuration
        defaultConfig.put("java.path", applicationProperties.getJava().getPath());
        defaultConfig.put("java.min-ram", applicationProperties.getJava().getMinRam());
        defaultConfig.put("java.max-ram", applicationProperties.getJava().getMaxRam());

        // Paths configuration
        defaultConfig.put("paths.servers", pathsConfiguration.getServersDir().toString());
        defaultConfig.put("paths.backups", pathsConfiguration.getBackupsDir().toString());

        // EULA configuration
        defaultConfig.put("eula.auto-accept", applicationProperties.getEula().isAutoAccept());
    }

    private void loadUserConfig() {
        userConfig = new Properties();
        Path configFile = pathsConfiguration.getConfigFile();

        if (Files.exists(configFile)) {
            try {
                userConfig.load(Files.newInputStream(configFile));
            } catch (IOException e) {
                System.out.println(TextDecorationUtil.error("Error loading user configuration: " + e.getMessage()));
            }
        } else {
            System.out.println(TextDecorationUtil.info("User configuration file not found, creating default configuration..."));
            createDefaultUserConfig();
            try {
                userConfig.load(Files.newInputStream(configFile));
            } catch (IOException e) {
                System.out.println(TextDecorationUtil.error("Error loading user configuration after creating default file: " + e.getMessage()));
            }
        }
        boolean updated = false;
        for (String key : defaultConfig.keySet()) {
            if (!userConfig.containsKey(key)) {
                userConfig.setProperty(key, String.valueOf(defaultConfig.get(key)));
                updated = true;
            }
        }
        if (updated) {
            saveUserConfig();
        }
        validateAndFixUserConfig();
    }

    private void validateAndFixUserConfig() {
        boolean changed = false;
        for (String key : userConfig.stringPropertyNames()) {
            String value = userConfig.getProperty(key);
            IValidator<String> validator = getValidatorForKey(key);
            if (validator != null) {
                ValidationResult result = validator.validate(value);
                if (result.isNotValid()) {
                    Object defaultValue = defaultConfig.get(key);
                    System.out.println(TextDecorationUtil.error("Invalid configuration for '" + key + "': " + value + ". Resetting to default value: " + defaultValue));
                    if (defaultValue != null) {
                        userConfig.setProperty(key, String.valueOf(defaultValue));
                        System.out.println("[MineControl] The configuration '" + key + "' was invalid ('" + value + "') and has been reset to the default value: '" + defaultValue + "'.");
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            saveUserConfig();
        }
    }

    private void createDefaultUserConfig() {
        try {
            Properties defaultUserConfig = new Properties();
            for (Map.Entry<String, Object> entry : defaultConfig.entrySet()) {
                defaultUserConfig.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
            }
            // Update
            defaultUserConfig.setProperty("update.check-on-startup", String.valueOf(applicationProperties.getUpdate().isCheckOnStartup()));
            // Paths
            defaultUserConfig.setProperty("paths.servers", pathsConfiguration.getServersDir().toString());
            defaultUserConfig.setProperty("paths.backups", pathsConfiguration.getBackupsDir().toString());
            // Java
            defaultUserConfig.setProperty("java.path", applicationProperties.getJava().getPath());
            defaultUserConfig.setProperty("java.min-ram", applicationProperties.getJava().getMinRam());
            defaultUserConfig.setProperty("java.max-ram", applicationProperties.getJava().getMaxRam());
            // EULA
            defaultUserConfig.setProperty("eula.auto-accept", String.valueOf(applicationProperties.getEula().isAutoAccept()));

            Path configFile = pathsConfiguration.getConfigFile();
            Files.createDirectories(configFile.getParent());
            defaultUserConfig.store(Files.newOutputStream(configFile), "MineControl CLI - User Configuration");
        } catch (IOException e) {
            System.out.println(TextDecorationUtil.error("Error creating default user configuration file: " + e.getMessage()));
        }
    }

    private void createCustomDirectories() {
        try {
            FileUtil.createDirectoryIfNotExists(getString("paths.servers"));
            FileUtil.createDirectoryIfNotExists(getString("paths.backups"));
            FileUtil.createDirectoryIfNotExists(getString("paths.temp"));
            FileUtil.createDirectoryIfNotExists(getString("paths.logs"));
        } catch (Exception e) {
            System.out.println(TextDecorationUtil.error("Could not create directories: " + e.getMessage()));
        }
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        if (userConfig.containsKey(key)) {
            return userConfig.getProperty(key);
        }
        if (defaultConfig.containsKey(key)) {
            return String.valueOf(defaultConfig.get(key));
        }
        return defaultValue;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    public boolean setProperty(String key, String value) {
        IValidator<String> validator = getValidatorForKey(key);
        if (validator != null) {
            ValidationResult result = validator.validate(value);
            if (result.isNotValid()) {
                return false;
            }
        }
        userConfig.setProperty(key, value);
        saveUserConfig();
        return true;
    }

    private IValidator<String> getValidatorForKey(String key) {
        if (key.equals("java.path")) {
            return new JavaPathValidator();
        } else if (key.equals("java.min-ram") || key.equals("java.max-ram")) {
            return new RamValidator();
        } else if (key.startsWith("paths.")) {
            return path -> {
                DirectoryValidator dirValidator = new DirectoryValidator();
                return dirValidator.validate(Path.of(path));
            };
        }
        return null;
    }

    private void saveUserConfig() {
        try {
            Path configFile = pathsConfiguration.getConfigFile();
            userConfig.store(Files.newOutputStream(configFile), "MineControl CLI - User Configuration");
        } catch (IOException e) {
            System.err.println(TextDecorationUtil.error("Error saving user configuration: " + e.getMessage()));
        }
    }

    public void resetToDefaults() {
        userConfig.clear();
        for (Map.Entry<String, Object> entry : defaultConfig.entrySet()) {
            userConfig.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
        }
        saveUserConfig();
    }

    public Map<String, String> getAllProperties() {
        Map<String, String> allProperties = new HashMap<>();
        defaultConfig.forEach((key, value) -> allProperties.put(key, String.valueOf(value)));
        userConfig.forEach((key, value) -> allProperties.put(String.valueOf(key), String.valueOf(value)));
        return allProperties;
    }

    public boolean hasProperty(String key) {
        return userConfig.containsKey(key) || defaultConfig.containsKey(key);
    }
}