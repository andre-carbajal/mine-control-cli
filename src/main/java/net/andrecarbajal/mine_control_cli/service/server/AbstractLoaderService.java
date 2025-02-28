package net.andrecarbajal.mine_control_cli.service.server;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.AppConfiguration;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.io.FileUtil;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.style.TemplateExecutor;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractLoaderService {

    protected final AppConfiguration appConfiguration;

    protected final FileUtil fileUtil;

    protected final FileDownloadService fileDownloadService;

    protected abstract String getApiUrl();

    protected abstract List<String> getVersions();

    protected String selectVersion(Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        List<SelectorItem<String>> items = getVersions().stream()
                .map(version -> SelectorItem.of(version, version))
                .toList();

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(terminal, items, "Minecraft Version", null);
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);
        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(SingleItemSelector.SingleItemSelectorContext.empty());

        return context.getResultItem()
                .flatMap(si -> Optional.ofNullable(si.getItem()))
                .orElseThrow(() -> new IllegalStateException("No version selected"));
    }

    protected Path prepareServerDirectory(String serverName) {
        Path serverPath = appConfiguration.getInstancesPath().resolve(serverName);
        fileUtil.createFolder(serverPath);
        return serverPath;
    }

    protected void acceptEula(Path serverPath) {
        fileUtil.saveEulaFile(serverPath);
    }

    protected void logServerCreationSuccess(String serverName, String version) {
        System.out.println("Server created successfully: " + serverName + " (version: " + version + ")");
    }

    protected void deleteServerDirectory(Path serverPath) {
        fileUtil.deleteFolder(serverPath);
    }
}
