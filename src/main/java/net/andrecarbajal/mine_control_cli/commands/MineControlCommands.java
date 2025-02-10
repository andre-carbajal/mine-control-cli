package net.andrecarbajal.mine_control_cli.commands;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class MineControlCommands {

    @ShellMethod(key = "create", value = "Create a new server")
    public void create() {
    }

    @ShellMethod(key = {"ls", "list"}, value = "List all the servers")
    public void list() {
    }

    @ShellMethod(key = "delete", value = "Delete a server")
    public void delete() {
    }

    @ShellMethod(key = "start", value = "Start a server")
    public void start() {
    }
}
