package net.andrecarbajal.mine_control_cli.config;

import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.ProgressBar;
import org.jline.terminal.Terminal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class MineControlConfig {
    @Bean
    public String init() {
        Path mineControlCliFolder = FileUtil.getMineControlCliFolder();
        Path serverInstancesFolder = FileUtil.getServerInstancesFolder();
        Path serverBackupsFolder = FileUtil.getServerBackupsFolder();

        if (Files.notExists(mineControlCliFolder)) {
            try {
                Files.createDirectories(mineControlCliFolder);
            } catch (Exception e) {
                throw new RuntimeException("Error creating mine-control-cli folder", e);
            }
        }

        if (Files.notExists(serverInstancesFolder)) {
            try {
                Files.createDirectories(serverInstancesFolder);
            } catch (Exception e) {
                throw new RuntimeException("Error creating server instances folder", e);
            }
        }

        if (Files.notExists(serverBackupsFolder)) {
            try {
                Files.createDirectories(serverBackupsFolder);
            } catch (Exception e) {
                throw new RuntimeException("Error creating server backups folder", e);
            }
        }

        return "mine-control-cli started";
    }

    @Bean
    public ProgressBar progressBar(Terminal terminal) {
        return new ProgressBar(terminal);
    }
}
