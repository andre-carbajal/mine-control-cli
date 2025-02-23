package net.andrecarbajal.mine_control_cli.config;

import lombok.AllArgsConstructor;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.validator.ConfigValidator;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

@Component
@AllArgsConstructor
public class DefaultConfig {
    private final FileUtil fileUtil;

    private final ConfigValidator validator = new ConfigValidator();

    public Properties loadConfig() throws IOException {
        Path configPath = fileUtil.getConfiguration();
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

    private void setDefaultProperties(Properties properties) {
        properties.setProperty("server.ram", "2G");
        properties.setProperty("java.path", "java");
    }
}