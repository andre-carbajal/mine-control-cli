package net.andrecarbajal.mine_control_cli.commands;

import net.andrecarbajal.mine_control_cli.model.LoaderType;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class LoaderCommands {

    @ShellMethod(key = {"loader list", "loader ls"}, value = "List all the server loaders")
    public void loaders() {
        System.out.println(TextDecorationUtil.green("=== Available Server Loaders ==="));
        for (LoaderType loader : LoaderType.values()) {
            System.out.println(TextDecorationUtil.cyan(String.format("  %2d.", loader.ordinal() + 1)) + " " + loader.getDisplayName());
        }
        System.out.println(TextDecorationUtil.green("==============================="));
    }
}
