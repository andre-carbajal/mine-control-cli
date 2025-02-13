package net.andrecarbajal.mine_control_cli.commands;

import net.andrecarbajal.mine_control_cli.model.ServerLoader;
import net.andrecarbajal.mine_control_cli.service.process.ServerProcessManager;
import net.andrecarbajal.mine_control_cli.service.server.PaperService;
import net.andrecarbajal.mine_control_cli.service.server.SnapshotService;
import net.andrecarbajal.mine_control_cli.service.server.VanillaService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.validator.FolderNameValidator;
import net.andrecarbajal.mine_control_cli.validator.ServerFileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.component.ConfirmationInput;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@Command
@SuppressWarnings("unused")
public class MineControlCommands extends AbstractShellComponent {

    @Autowired
    private FolderNameValidator folderNameValidator;

    @Autowired
    private ServerFileValidator serverFileValidator;

    @Autowired
    private ServerProcessManager serverProcessManager;

    @Autowired
    private VanillaService vanillaService;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private PaperService paperService;

    @Command(command = "create", description = "Create a new server")
    public void create(
            @Option(description = "The name of the server") String name,
            @Option(description = "The server loader type") String serverLoader) {

        name = getValidatedServerName(name);
        ServerLoader loader = getSelectedLoader(serverLoader);

        System.out.printf("Creating new %s server with name: %s\n", loader, name);

        try {
            switch (loader) {
                case VANILLA:
                    vanillaService.createServer(name, getTerminal(), getResourceLoader(), getTemplateExecutor());
                    break;
                case SNAPSHOT:
                    snapshotService.createServer(name, getTerminal(), getResourceLoader(), getTemplateExecutor());
                    break;
                case PAPER:
                    paperService.createServer(name, getTerminal(), getResourceLoader(), getTemplateExecutor());
                    break;
            }
            System.out.printf("Server %s created successfully\n", name);
        } catch (Exception e) {
            throw new RuntimeException("Server creation failed", e);
        }
    }

    @Command(command = "list", alias = "ls", description = "List all the servers")
    public void list() {
        try {
            List<String> servers = FileUtil.getFilesInFolder(FileUtil.getServerInstancesFolder());

            if (servers.isEmpty()) {
                System.out.println("No servers found");
                return;
            }

            System.out.println("Available servers:");
            servers.stream().map(server -> String.format("\t%d. %s", servers.indexOf(server) + 1, server)).forEach(System.out::println);
        } catch (Exception e) {
            throw new RuntimeException("Failed to list servers", e);
        }
    }

    @Command(command = "delete", description = "Delete a server")
    public void delete() {
        try {
            String serverToDelete = selectServer("Select server to delete");
            if (serverToDelete == null) return;

            if (confirmDeletion(serverToDelete)) {
                Path serverPath = FileUtil.getServerInstancesFolder().resolve(serverToDelete);
                FileUtil.deleteFolder(serverPath);
                System.out.printf("Server %s deleted successfully\n", serverToDelete);
            } else {
                System.out.printf("Server %s deletion cancelled\n", serverToDelete);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete server", e);
        }
    }

    @Command(command = "start", description = "Start a server")
    public void start() {
        try {
            String serverToStart = selectServer("Select server to start");
            if (serverToStart == null) return;

            Path serverPath = FileUtil.getServerInstancesFolder().resolve(serverToStart);
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

    @Command(command = "loaders", description = "List all the server loaders")
    public void loaders() {
        System.out.println("Available server loaders:");
        for (ServerLoader loader : ServerLoader.values()) {
            System.out.printf("\t%d. %s\n", loader.ordinal() + 1, StringUtils.capitalize(loader.name().toLowerCase()));
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
        List<String> servers = FileUtil.getFilesInFolder(FileUtil.getServerInstancesFolder());

        if (servers.isEmpty()) {
            System.out.println("No servers available");
            return null;
        }

        List<SelectorItem<String>> items = servers.stream().map(server -> SelectorItem.of(server, server)).toList();

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(getTerminal(), items, prompt, null);
        selector.setResourceLoader(getResourceLoader());
        selector.setTemplateExecutor(getTemplateExecutor());

        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(SingleItemSelector.SingleItemSelectorContext.empty());

        return context.getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).orElse(null);
    }

    private boolean confirmDeletion(String serverName) {
        ConfirmationInput confirmation = new ConfirmationInput(getTerminal(), String.format("Do you want to delete server '%s'?", serverName), false);
        confirmation.setResourceLoader(getResourceLoader());
        confirmation.setTemplateExecutor(getTemplateExecutor());

        return confirmation.run(ConfirmationInput.ConfirmationInputContext.empty()).getResultValue();
    }
}
