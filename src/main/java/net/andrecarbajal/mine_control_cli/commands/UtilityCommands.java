package net.andrecarbajal.mine_control_cli.commands;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.command.annotation.Command;
import org.springframework.stereotype.Component;

@Command
@Component
@SuppressWarnings("unused")
@AllArgsConstructor
public class UtilityCommands {
    private final ConfigurableApplicationContext context;

    @Command(command = "exit", alias = {"quit"}, description = "Exit the application")
    public void exit() {
        SpringApplication.exit(context, () -> 0);
        System.exit(0);
    }
}

