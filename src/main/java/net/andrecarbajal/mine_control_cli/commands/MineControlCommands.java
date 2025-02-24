package net.andrecarbajal.mine_control_cli.commands;

import lombok.AllArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.MineControlConfig;
import net.andrecarbajal.mine_control_cli.model.ServerLoader;
import net.andrecarbajal.mine_control_cli.service.process.ServerProcessManager;
import net.andrecarbajal.mine_control_cli.service.server.FabricService;
import net.andrecarbajal.mine_control_cli.service.server.PaperService;
import net.andrecarbajal.mine_control_cli.service.server.SnapshotService;
import net.andrecarbajal.mine_control_cli.service.server.VanillaService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.ZipUtils;
import net.andrecarbajal.mine_control_cli.validator.file.FolderNameValidator;
import net.andrecarbajal.mine_control_cli.validator.file.ServerFileValidator;
import org.springframework.shell.component.ConfirmationInput;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ShellComponent
@AllArgsConstructor
public class MineControlCommands extends AbstractShellComponent {
    private MineControlConfig config;
    private FolderNameValidator folderNameValidator;
    private ServerFileValidator serverFileValidator;
    private FileUtil fileUtil;
    private ZipUtils zipUtils;
    private ServerProcessManager serverProcessManager;
    private VanillaService vanillaService;
    private SnapshotService snapshotService;
    private FabricService fabricService;
    private PaperService paperService;

    @ShellMethod(key = "create", value = "Create a new server")
    public void create(
            @ShellOption(help = "The name of the server", defaultValue = ShellOption.NULL) String name,
            @ShellOption(help = "The server loader type", defaultValue = ShellOption.NULL) String serverLoader,
            @ShellOption(help = "The minecraft version", defaultValue = ShellOption.NULL) String version,
            @ShellOption(help = "The loader version", defaultValue = ShellOption.NULL) String loaderVersion) {

        name = getValidatedServerName(name);
        ServerLoader loader = getSelectedLoader(serverLoader);

        System.out.printf("Creating new %s server with name: %s\n", loader, name);

        switch (loader) {
            case VANILLA:
                vanillaService.createServer(ServerLoader.VANILLA, name, version, getTerminal(), getResourceLoader(), getTemplateExecutor());
                break;
            case SNAPSHOT:
                snapshotService.createServer(ServerLoader.SNAPSHOT, name, version, getTerminal(), getResourceLoader(), getTemplateExecutor());
                break;
            case FABRIC:
                fabricService.createServer(ServerLoader.FABRIC, name, version, loaderVersion, getTerminal(), getResourceLoader(), getTemplateExecutor());
                break;
            case PAPER:
                paperService.createServer(ServerLoader.PAPER, name, version, getTerminal(), getResourceLoader(), getTemplateExecutor());
                break;
        }
    }

    @ShellMethod(key = {"list", "ls"}, value = "List all the servers")
    public void list() {
        try {
            List<String[]> servers = fileUtil.getFilesInFolderWithDetails(config.getInstancesPath());

            if (servers.isEmpty()) {
                System.out.println("No servers found");
                return;
            }

            System.out.println("Available servers:");
            servers.stream()
                    .map(server -> String.format("\t%d. %s", servers.indexOf(server) + 1, server[1]))
                    .forEach(System.out::println);
        } catch (Exception e) {
            throw new RuntimeException("Failed to list servers", e);
        }
    }

    @ShellMethod(key = {"delete", "rm", "remove"}, value = "Delete a server")
    public void delete() {
        try {
            String serverToDelete = selectServer("Select server to delete");
            if (serverToDelete == null) return;

            if (confirmDeletion(serverToDelete)) {
                Path serverPath = config.getInstancesPath().resolve(serverToDelete);
                fileUtil.deleteFolder(serverPath);
                System.out.printf("Server %s deleted successfully\n", serverToDelete);
            } else {
                System.out.printf("Server %s deletion cancelled\n", serverToDelete);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete server", e);
        }
    }

    @ShellMethod(key = "start", value = "Start a server")
    public void start() {
        try {
            String serverToStart = selectServer("Select server to start");
            if (serverToStart == null) return;

            Path serverPath = config.getInstancesPath().resolve(serverToStart);
            Path jarFilePath = serverPath.resolve("server.jar");

            if (!serverFileValidator.isValid(jarFilePath.toString())) {
                System.out.println("Server files are invalid or missing");
                return;
            }

            System.out.printf("Starting server: %s\n", serverToStart);
            serverProcessManager.startServer(serverPath, jarFilePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start server", e);
        }
    }

    @ShellMethod(key = "loaders", value = "List all the server loaders")
    public void loaders() {
        System.out.println("Available server loaders:");
        for (ServerLoader loader : ServerLoader.values()) {
            System.out.printf("\t%d. %s\n", loader.ordinal() + 1, StringUtils.capitalize(loader.name().toLowerCase()));
        }
    }

    @ShellMethod(key = "backup", value = "Backup a server")
    public void backup() {
        try {
            String serverToBackup = selectServer("Select sever to backup");
            if (serverToBackup == null) return;

            Path serverPath = config.getInstancesPath().resolve(serverToBackup);
            String fileName = serverToBackup + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
            Path zipFilePath = config.getBackupsPath().resolve(fileName + ".zip");

            zipUtils.zipFolder(serverPath.toString(), zipFilePath.toString());

            System.out.println("Server backed up successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to backup server", e);
        }
    }

    private String getValidatedServerName(String name) {
        if (name != null && folderNameValidator.isValid(name)) {
            return name;
        }

        StringInput input = new StringInput(getTerminal());
        input.setResourceLoader(getResourceLoader());
        input.setTemplateExecutor(getTemplateExecutor());

        do {
            if (name != null) {
                System.out.println("Invalid server name. Please enter a valid name:");
            } else {
                System.out.println("Please enter the server name:");
            }

            StringInput.StringInputContext context = input.run(StringInput.StringInputContext.empty());
            name = context.getResultValue();
        } while (!folderNameValidator.isValid(name));

        return name;
    }

    private ServerLoader getSelectedLoader(String loaderName) {
        if (loaderName != null) {
            try {
                return ServerLoader.valueOf(loaderName.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.printf("Invalid loader name provided: %s\n", loaderName);
            }
        }

        List<SelectorItem<ServerLoader>> items = Stream.of(ServerLoader.values()).map(loader -> SelectorItem.of(loader.name(), loader)).toList();

        SingleItemSelector<ServerLoader, SelectorItem<ServerLoader>> selector = new SingleItemSelector<>(getTerminal(), items, "Server Loader", null);
        selector.setResourceLoader(getResourceLoader());
        selector.setTemplateExecutor(getTemplateExecutor());

        SingleItemSelector.SingleItemSelectorContext<ServerLoader, SelectorItem<ServerLoader>> context = selector.run(SingleItemSelector.SingleItemSelectorContext.empty());

        return context.getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).orElse(ServerLoader.VANILLA);
    }

    private String selectServer(String prompt) {
        List<String[]> servers = fileUtil.getFilesInFolderWithDetails(config.getInstancesPath());

        if (servers.isEmpty()) {
            System.out.println("No servers available");
            return null;
        }

        List<SelectorItem<String>> items = servers.stream()
                .map(server -> SelectorItem.of(server[0], server[1]))
                .toList();

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(getTerminal(), items, prompt, null);
        selector.setResourceLoader(getResourceLoader());
        selector.setTemplateExecutor(getTemplateExecutor());

        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(SingleItemSelector.SingleItemSelectorContext.empty());

        return context.getResultItem().flatMap(si -> Optional.ofNullable(si.getName())).orElse(null);
    }

    private boolean confirmDeletion(String serverName) {
        ConfirmationInput confirmation = new ConfirmationInput(getTerminal(), String.format("Do you want to delete server '%s'?", serverName), false);
        confirmation.setResourceLoader(getResourceLoader());
        confirmation.setTemplateExecutor(getTemplateExecutor());

        return confirmation.run(ConfirmationInput.ConfirmationInputContext.empty()).getResultValue();
    }
}
