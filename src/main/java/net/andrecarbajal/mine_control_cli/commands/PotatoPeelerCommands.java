package net.andrecarbajal.mine_control_cli.commands;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.service.PotatoPeelerService;
import net.andrecarbajal.mine_control_cli.service.ServerManagerService;
import net.andrecarbajal.mine_control_cli.util.ComponentUtil;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class PotatoPeelerCommands extends AbstractShellComponent {
    private final ServerManagerService serverManagerService;
    private final ConfigurationManager configurationManager;
    private final PotatoPeelerService potatoPeelerService;

    @ShellMethod(key = "remove-unused-chunks", value = "Remove unused chunks from the server")
    public void removeUnusedChunks(@ShellOption(help = "The server to remove unused chunks from", defaultValue = ShellOption.NULL) String serverName) {
        if (serverName == null) {
            List<String> servers = serverManagerService.listServers();
            serverName = ComponentUtil.selectServer(servers, "Select the server to remove unused chunks from:", getTerminal(), getResourceLoader(), getTemplateExecutor());
            if (serverName == null) {
                System.out.println(TextDecorationUtil.error("No server selected for removing unused chunks."));
                return;
            }
        }
        String serversPath = configurationManager.getString("paths.servers");
        if (FileUtil.directoryNotExists(serversPath, serverName)) {
            System.out.println(TextDecorationUtil.error("Server '" + serverName + "' does not exist."));
            return;
        }
        File serverDir = new File(serversPath, serverName);
        String loaderType = serverManagerService.getLoaderType(serverDir);
        potatoPeelerService.removeUnusedChunks(serverName, loaderType);
    }
}
