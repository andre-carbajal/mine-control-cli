package net.andrecarbajal.mine_control_cli.commands;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@AllArgsConstructor
public class UtilityCommands {
    private final ConfigurableApplicationContext context;

    @ShellMethod(key = {"exit", "quit"}, value = "Exit the application")
    public void exit() {
        SpringApplication.exit(context, () -> 0);
        System.exit(0);
    }
}

