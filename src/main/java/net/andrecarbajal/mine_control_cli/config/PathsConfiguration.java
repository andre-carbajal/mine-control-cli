package net.andrecarbajal.mine_control_cli.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.andrecarbajal.mine_control_cli.config.properties.ApplicationProperties;
import net.andrecarbajal.mine_control_cli.config.properties.SpringApplicationProperties;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.SystemPathsUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class PathsConfiguration {
    private final SpringApplicationProperties springApplicationProperties;
    private final ApplicationProperties applicationProperties;
    private Path baseDir;

    @Value("${app.base-dir-suffix:Dev}")
    private String baseDirSuffix;

    @EventListener(ContextRefreshedEvent.class)
    public void initializePaths() {
        try {
            FileUtil.createDirectoryIfNotExists(getBaseDir());
            FileUtil.createDirectoryIfNotExists(getServersDir());
            FileUtil.createDirectoryIfNotExists(getBackupsDir());
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize directory structure", e);
        }
    }

    public Path getBaseDir() {
        if (baseDir == null) {
            baseDir = determineBaseDirectory();
        }
        return baseDir;
    }

    private Path determineBaseDirectory() {
        return SystemPathsUtil.getSystemConfigDir(springApplicationProperties.getName() + baseDirSuffix);
    }

    public Path getServersDir() {
        return getBaseDir().resolve(applicationProperties.getPaths().getServersDir());
    }

    public Path getBackupsDir() {
        return getBaseDir().resolve(applicationProperties.getPaths().getBackupsDir());
    }

    public Path getConfigFile() {
        return getBaseDir().resolve("minecontrol.properties");
    }
}
