package net.andrecarbajal.mine_control_cli.commands;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class MineControlCommands {

    @ShellMethod(key = "hello", value = "Prints 'Hello, World!'")
    public String hello() {
        return "Hello, World!";
    }
}
