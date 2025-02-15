package net.andrecarbajal.mine_control_cli.config;

import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.validator.ConfigValidator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class DefaultConfig {
    private static final ConfigValidator validator = new ConfigValidator();

    public static Properties loadConfig() throws IOException {
        Path configPath = FileUtil.getConfiguration();
        Properties properties = new Properties();

        if (configPath.toFile().exists()) {
            try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
                properties.load(fis);
                validator.validateConfig(properties);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                properties.clear();
                setDefaultProperties(properties);
                try (FileOutputStream fos = new FileOutputStream(configPath.toFile())) {
                    properties.store(fos, "Reset to default after validation failure");
                }
            }
        } else {
            setDefaultProperties(properties);
            configPath.getParent().toFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(configPath.toFile())) {
                properties.store(fos, "Default config");
            }
            System.out.println("Config file created at: " + configPath);
        }
        return properties;
    }

    private static void setDefaultProperties(Properties properties) {
        properties.setProperty("server.ram", "2G");
    }
}