package net.andrecarbajal.mine_control_cli.config;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.service.UpdateService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateCheckRunner {
    private final UpdateService updateService;

    @Bean
    public String checkUpdate() {
        updateService.showUpdateMessage(false);
        return "Update check completed.";
    }
}

