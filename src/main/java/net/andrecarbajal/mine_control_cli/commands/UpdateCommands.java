package net.andrecarbajal.mine_control_cli.commands;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.service.UpdateService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@RequiredArgsConstructor
public class UpdateCommands {
    private final UpdateService updateService;

    @ShellMethod(key = {"update check"}, value = "Check if a new version is available")
    public void check() {
        updateService.showUpdateMessage(true);
    }
}
