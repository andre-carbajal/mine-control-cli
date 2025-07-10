package net.andrecarbajal.mine_control_cli.config;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.util.ProgressBar;
import org.jline.terminal.Terminal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ProgressBarRunner {
    @Bean
    public ProgressBar progressBar(Terminal terminal) {
        return new ProgressBar(terminal);
    }
}
