package net.andrecarbajal.mine_control_cli.commands;

import net.andrecarbajal.mine_control_cli.config.ServerLoader;
import net.andrecarbajal.mine_control_cli.service.minecraft.PaperService;
import net.andrecarbajal.mine_control_cli.service.minecraft.SnapshotService;
import net.andrecarbajal.mine_control_cli.service.minecraft.VanillaService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.validator.FolderNameValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.shell.component.ConfirmationInput;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Component
@Command
@SuppressWarnings("unused")
public class MineControlCommands extends AbstractShellComponent {

    @Autowired
    private FolderNameValidator folderNameValidator;

    @Autowired
    private VanillaService vanillaService;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private PaperService paperService;

    @Command(command = "create", description = "Create a new server")
    public void create(@Option(description = "The name of the server") String name, @Option(description = "The server loader") String serverLoader) {
        while (name == null || !folderNameValidator.isValid(name)) {
            if (name != null) {
                System.out.println("Invalid folder name. Please enter a valid name:");
            } else {
                System.out.println("Please enter the name of the server:");
            }

            StringInput input = new StringInput(getTerminal());
            input.setResourceLoader(getResourceLoader());
            input.setTemplateExecutor(getTemplateExecutor());
            StringInput.StringInputContext context = input.run(StringInput.StringInputContext.empty());
            name = context.getResultValue();
        }

        if (serverLoader == null || ServerLoader.getLoader(serverLoader) == null) {
            List<SelectorItem<String>> items = ServerLoader.getStringLoader().stream().map(loader -> SelectorItem.of(loader, loader)).toList();

            SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(getTerminal(), items, "Server Loader", null);
            selector.setResourceLoader(getResourceLoader());
            selector.setTemplateExecutor(getTemplateExecutor());
            SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(SingleItemSelector.SingleItemSelectorContext.empty());
            serverLoader = context.getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).get();
        }


        switch (ServerLoader.getLoader(serverLoader)) {
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
    }

    @Command(command = "list", alias = "ls", description = "List all the servers")
    public void list() {
        System.out.println("Available servers:");
        List<String> servers = FileUtil.getFilesInFolder(FileUtil.getMineControlCliFolder());
        servers.stream().map(server -> "\t" + (servers.indexOf(server) + 1) + ". " + server).forEach(System.out::println);
    }

    @Command(command = "delete", description = "Delete a server")
    public void delete() {
        List<SelectorItem<String>> items = FileUtil.getFilesInFolder(FileUtil.getMineControlCliFolder()).stream()
                .map(version -> SelectorItem.of(version, version))
                .toList();

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(getTerminal(), items, "Server", null);
        selector.setResourceLoader(getResourceLoader());
        selector.setTemplateExecutor(getTemplateExecutor());
        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector
                .run(SingleItemSelector.SingleItemSelectorContext.empty());
        String server = context.getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).get();

        ConfirmationInput component = new ConfirmationInput(getTerminal(), "Do you want to delete " + server, false);
        component.setResourceLoader(getResourceLoader());
        component.setTemplateExecutor(getTemplateExecutor());
        ConfirmationInput.ConfirmationInputContext confirmationInputContext = component.run(ConfirmationInput.ConfirmationInputContext.empty());

        if (confirmationInputContext.getResultValue()) {
            FileUtil.deleteFolder(Paths.get(FileUtil.getMineControlCliFolder().toString(), server));
            System.out.println(server + " deleted successfully");
        }else {
            System.out.println(server + " not deleted");
        }
    }

    @Command(command = "start", description = "Start a server")
    public void start() {
    }

    @Command(command = "loaders", description = "List all the server loaders")
    public void loaders() {
        System.out.println("Available server loaders:");
        List<String> loaders = ServerLoader.getStringLoader();
        loaders.stream().map(loader -> "\t" + (loaders.indexOf(loader) + 1) + ". " + loader).forEach(System.out::println);
    }
}
