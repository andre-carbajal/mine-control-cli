package net.andrecarbajal.mine_control_cli.service.server;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.style.TemplateExecutor;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractLoaderService {

    protected final FileDownloadService fileDownloadService;

    protected final RestTemplate restTemplate = new RestTemplate();

    public abstract void createServer(String serverName, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor);

    protected abstract String getApiUrl();

    protected abstract List<String> getVersions();

    protected String selectVersion(Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        List<SelectorItem<String>> items = getVersions().stream().map(version -> SelectorItem.of(version, version)).toList();

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(terminal, items, "Minecraft Version", null);
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);
        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(SingleItemSelector.SingleItemSelectorContext.empty());
        return context.getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).get();
    }

    protected Path prepareServerDirectory(String serverName) {
        Path serverPath = FileUtil.getServerInstancesFolder().resolve(serverName);
        FileUtil.createFolder(serverPath);
        return serverPath;
    }

    protected void acceptEula(Path serverPath) {
        FileUtil.saveEulaFile(serverPath);
    }

    protected void logServerCreationSuccess(String serverName, String version) {
        System.out.println("Server created successfully: " + serverName + " (version: " + version + ")");
    }

    protected void deleteServerDirectory(Path serverPath) {
        FileUtil.deleteFolder(serverPath);
    }
}
