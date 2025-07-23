package net.andrecarbajal.mine_control_cli.commands;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.model.LoaderType;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.service.ServerManagerService;
import net.andrecarbajal.mine_control_cli.service.ServerProcessService;
import net.andrecarbajal.mine_control_cli.service.server.base.ServerCreator;
import net.andrecarbajal.mine_control_cli.service.server.factory.ServerCreatorFactory;
import net.andrecarbajal.mine_control_cli.util.ComponentUtil;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class ServerCommands extends AbstractShellComponent {
    private final ConfigurationManager configurationManager;
    private final DownloadService downloadService;
    private final ServerProcessService serverProcessService;
    private final ServerManagerService serverManagerService;

    @ShellMethod(key = {"server create", "sc"}, value = "Create a new server")
    public void create(
            @ShellOption(help = "Name of the server to create", defaultValue = ShellOption.NULL) String serverName,
            @ShellOption(help = "Type of server loader", defaultValue = ShellOption.NULL) String loaderType,
            @ShellOption(help = "Loader version", defaultValue = ShellOption.NULL) String loaderVersion,
            @ShellOption(help = "Minecraft version for the server", defaultValue = ShellOption.NULL) String minecraftVersion) {
        if (serverName == null) {
            serverName = ComponentUtil.inputString("Enter the name of the server to create:", getTerminal(), getResourceLoader(), getTemplateExecutor());
            if (serverName == null || serverName.isBlank()) {
                System.out.println(TextDecorationUtil.error("Server name cannot be empty."));
                return;
            }
        }

        LoaderType selectedLoaderType = null;
        if (loaderType != null) {
            for (LoaderType type : LoaderType.values()) {
                if (type.name().equalsIgnoreCase(loaderType)) {
                    selectedLoaderType = type;
                    break;
                }
            }
            if (selectedLoaderType == null) {
                selectedLoaderType = ComponentUtil.selectLoaderType(getTerminal(), getResourceLoader(), getTemplateExecutor());
            }
        } else {
            selectedLoaderType = ComponentUtil.selectLoaderType(getTerminal(), getResourceLoader(), getTemplateExecutor());
        }

        ServerCreator creator = ServerCreatorFactory.getCreator(selectedLoaderType, configurationManager, downloadService, serverProcessService);
        creator.createServer(serverName, loaderVersion, minecraftVersion, getTerminal(), getResourceLoader(), getTemplateExecutor());
    }

    @ShellMethod(key = {"server list", "sl"}, value = "List all available servers")
    public void list() {
        List<String> serverDirs = serverManagerService.listServers();
        if (serverDirs.isEmpty()) {
            System.out.println(TextDecorationUtil.info("There are no servers available."));
            return;
        }
        System.out.println(TextDecorationUtil.green("=== Available Servers ==="));
        for (int i = 0; i < serverDirs.size(); i++) {
            System.out.println(TextDecorationUtil.cyan(String.format("  %2d.", i + 1)) + " " + serverDirs.get(i));
        }
        System.out.println(TextDecorationUtil.green("========================="));
    }

    @ShellMethod(key = {"server delete", "sd"}, value = "Delete a server")
    public void delete(
            @ShellOption(help = "Name of the server to delete", defaultValue = ShellOption.NULL) String serverName
    ) {
        if (serverName == null) {
            List<String> servers = serverManagerService.listServers();
            serverName = ComponentUtil.selectServer(servers, "Select the server to delete:", getTerminal(), getResourceLoader(), getTemplateExecutor());
            if (serverName == null) {
                System.out.println(TextDecorationUtil.error("No server selected for deletion."));
                return;
            }
        }

        if (ComponentUtil.confirm("Are you sure you want to delete the server '" + serverName + "'? This action cannot be undone.", getTerminal(), getResourceLoader(), getTemplateExecutor())) {
            serverManagerService.deleteServer(serverName);
        } else {
            System.out.println(TextDecorationUtil.info("Server deletion cancelled."));
        }
    }

    @ShellMethod(key = {"server start", "ss"}, value = "Start a server")
    public void start(
            @ShellOption(help = "Name of the server to start", defaultValue = ShellOption.NULL) String serverName
    ) {
        if (serverName == null) {
            List<String> servers = serverManagerService.listServers();
            serverName = ComponentUtil.selectServer(servers, "Select the server to start:", getTerminal(), getResourceLoader(), getTemplateExecutor());
            if (serverName == null) {
                System.out.println(TextDecorationUtil.error("No server selected for starting."));
                return;
            }
        }
        serverManagerService.startServer(serverName);
    }
}
