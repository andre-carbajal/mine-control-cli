package net.andrecarbajal.mine_control_cli.config;

import lombok.extern.slf4j.Slf4j;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Configuration
public class MineControlConfig {
    @Bean
    public String init() {
        Path path = FileUtil.getMineControlCliFolder();
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                log.error("Error creating mine-control-cli folder", e);
                throw new RuntimeException("Error creating mine-control-cli folder", e);
            }
        }

        return "mine-control-cli started";
    }
}
