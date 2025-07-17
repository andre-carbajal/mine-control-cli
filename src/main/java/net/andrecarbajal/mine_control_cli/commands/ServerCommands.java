package net.andrecarbajal.mine_control_cli.commands;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.model.LoaderType;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.service.ServerProcessService;
import net.andrecarbajal.mine_control_cli.service.server.base.ServerCreator;
import net.andrecarbajal.mine_control_cli.service.server.factory.ServerCreatorFactory;
import net.andrecarbajal.mine_control_cli.util.ComponentUtil;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.SystemPathsUtil;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Properties;

@ShellComponent
@RequiredArgsConstructor
public class ServerCommands extends AbstractShellComponent {
    private final ConfigurationManager configurationManager;
    private final DownloadService downloadService;
    private final ServerProcessService serverProcessService;

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
        String serversPath = configurationManager.getString("paths.servers");
        File serversDir = new File(serversPath);
        String[] serverDirsArray = serversDir.list((current, name) -> new File(current, name).isDirectory());
        if (serverDirsArray == null || serverDirsArray.length == 0) {
            System.out.println(TextDecorationUtil.info("There are no servers available."));
            return;
        }
        List<String> serverDirs = FileUtil.listDirectories(serversPath);
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
            serverName = ComponentUtil.selectServer("Select the server to delete:", configurationManager, getTerminal(), getResourceLoader(), getTemplateExecutor());
            if (serverName == null) {
                System.out.println(TextDecorationUtil.error("No server selected for deletion."));
                return;
            }
        }

        if (ComponentUtil.confirm("Are you sure you want to delete the server '" + serverName + "'? This action cannot be undone.", getTerminal(), getResourceLoader(), getTemplateExecutor())) {
            String serversPath = configurationManager.getString("paths.servers");
            File serverDir = new File(serversPath, serverName);
            if (FileUtil.directoryNotExists(serversPath, serverName)) {
                System.out.println(TextDecorationUtil.error("Server '" + serverName + "' does not exist."));
                return;
            }
            if (FileUtil.deleteDirectoryRecursively(serverDir)) {
                System.out.println(TextDecorationUtil.success("Server '" + serverName + "' deleted successfully."));
            } else {
                System.out.println(TextDecorationUtil.error("Failed to delete server '" + serverName + "'. Please check permissions or if the server is running."));
            }
        } else {
            System.out.println(TextDecorationUtil.info("Server deletion cancelled."));
        }
    }

    @ShellMethod(key = {"server start", "ss"}, value = "Start a server")
    public void start(
            @ShellOption(help = "Name of the server to start", defaultValue = ShellOption.NULL) String serverName
    ) {
        if (serverName == null) {
            serverName = ComponentUtil.selectServer("Select the server to delete:", configurationManager, getTerminal(), getResourceLoader(), getTemplateExecutor());
            if (serverName == null) {
                System.out.println(TextDecorationUtil.error("No server selected for deletion."));
                return;
            }
        }

        String serversPath = configurationManager.getString("paths.servers");
        if (FileUtil.directoryNotExists(serversPath, serverName)) {
            System.out.println(TextDecorationUtil.error("Server '" + serverName + "' does not exist."));
            return;
        }
        File serverDir = new File(serversPath, serverName);
        File infoFile = new File(serverDir, ".server-info.properties");
        String loaderType = "VANILLA";
        if (infoFile.exists()) {
            Properties props = new Properties();
            try (FileReader reader = new FileReader(infoFile)) {
                props.load(reader);
                loaderType = props.getProperty("loaderType", "VANILLA");
            } catch (Exception e) {
                System.out.println(TextDecorationUtil.error("Could not read .server-info.properties: " + e.getMessage()));
                return;
            }
        }
        if (loaderType.equalsIgnoreCase("NEOFORGE")) {
            if (startForgeBasedServer(serverDir, serverName, "NeoForge", "libraries/net/neoforged/neoforge")) {
                return;
            }
        }
        if (loaderType.equalsIgnoreCase("FORGE")){
            if (startForgeBasedServer(serverDir, serverName, "Forge", "libraries/net/minecraftforge/forge")) {
                return;
            }
        }
        File serverJar = new File(serverDir, "server.jar");
        if (!serverJar.exists()) {
            System.out.println(TextDecorationUtil.error("Server jar file not found in '" + serverDir.getAbsolutePath() + "'. Please ensure the server is set up correctly."));
            return;
        }
        System.out.println(TextDecorationUtil.info("Starting server '" + serverName + "'..."));
        serverProcessService.startServer(serverDir, serverJar.toPath());
    }

    private boolean startForgeBasedServer(File serverDir, String serverName, String loaderType, String librariesSubPath) {
        String osName = SystemPathsUtil.getOperatingSystemName().toLowerCase();
        String argsFileName = osName.contains("windows") ? "win_args.txt" : "unix_args.txt";
        File librariesDir = new File(serverDir, librariesSubPath);
        File[] versionDirs = librariesDir.listFiles(File::isDirectory);
        File foundArgsFile = null;
        if (versionDirs != null) {
            for (File vDir : versionDirs) {
                File candidate = new File(vDir, argsFileName);
                if (candidate.exists()) {
                    foundArgsFile = candidate;
                    break;
                }
            }
        }
        if (foundArgsFile == null) {
            System.out.println(TextDecorationUtil.error("No '" + argsFileName + "' found for " + loaderType + " in '" + librariesDir.getAbsolutePath() + "'."));
            return false;
        }
        System.out.println(TextDecorationUtil.info("Starting " + loaderType + " server '" + serverName + "'..."));
        serverProcessService.startForgeBasedServer(serverDir, foundArgsFile.toPath());
        return true;
    }
}
