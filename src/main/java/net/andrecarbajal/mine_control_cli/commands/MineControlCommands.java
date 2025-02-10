package net.andrecarbajal.mine_control_cli.commands;

import net.andrecarbajal.mine_control_cli.config.ServerLoader;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.standard.AbstractShellComponent;

import java.util.List;

@Command
@SuppressWarnings("unused")
public class MineControlCommands extends AbstractShellComponent {

    @Command(command = "create", description = "Create a new server")
    public void create(
            @Option(description = "The name of the server") String name,
            @Option(description = "The server loader", defaultValue = "VANILLA") ServerLoader loader) {

    }

    @Command(command = "list", alias = "ls", description = "List all the servers")
    public void list() {
    }

    @Command(command = "delete", description = "Delete a server")
    public void delete() {
    }

    @Command(command = "start", description = "Start a server")
    public void start() {
    }

    @Command(command = "loaders", description = "List all the server loaders")
    public void loaders() {
        System.out.println("Available server loaders:");
        List<String> loaders = ServerLoader.getLoaders();
        loaders.stream().map(loader -> "\t" + (loaders.indexOf(loader) + 1) + ". " + loader).forEach(System.out::println);
    }
}
