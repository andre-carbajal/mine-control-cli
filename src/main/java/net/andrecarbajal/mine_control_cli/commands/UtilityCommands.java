package net.andrecarbajal.mine_control_cli.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.command.annotation.Command;
import org.springframework.stereotype.Component;

@Command
@Component
@SuppressWarnings("unused")
public class UtilityCommands {
    private final ConfigurableApplicationContext context;

    @Autowired
    public UtilityCommands(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Command(command = "exit", alias = {"quit"}, description = "Exit the application")
    public void exit() {
        SpringApplication.exit(context, () -> 0);
        System.exit(0);
    }
}

