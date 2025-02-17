package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.model.ServerLoader;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.style.TemplateExecutor;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public abstract class AbstractModdedLoaderService extends AbstractLoaderService {

    public AbstractModdedLoaderService(FileDownloadService fileDownloadService) {
        super(fileDownloadService);
    }

    public void createServer(ServerLoader loader, String serverName, String version, String loaderVersion, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        Path serverPath = prepareServerDirectory(serverName);
        try {
            if (version == null || !getVersions().contains(version))
                version = selectVersion(terminal, resourceLoader, templateExecutor);
            if (loaderVersion == null || !getLoaderVersions().contains(loaderVersion))
                loaderVersion = selectLoaderVersion(terminal, resourceLoader, templateExecutor);
            downloadServerFiles(version, loaderVersion, serverPath);
            acceptEula(serverPath);
            saveServerInfo(serverPath, loader, version, loaderVersion);
            logServerCreationSuccess(serverName, version);
        } catch (Exception e) {
            deleteServerDirectory(FileUtil.getMineControlCliFolder().resolve(serverName));
        }
    }

    protected abstract List<String> getLoaderVersions();

    protected abstract String getDownloadUrl(String version, String loaderVersion);

    private String selectLoaderVersion(Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        List<SelectorItem<String>> items = getLoaderVersions().stream().map(version -> SelectorItem.of(version, version)).toList();

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(terminal, items, "Loader Version", null);
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);
        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(SingleItemSelector.SingleItemSelectorContext.empty());
        return context.getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).orElseThrow(() -> new IllegalStateException("No version loader selected"));
    }

    private void downloadServerFiles(String version, String loaderVersion, Path serverPath) {
        String downloadUrl = getDownloadUrl(version, loaderVersion);
        fileDownloadService.downloadFile(downloadUrl, serverPath, "server.jar");
    }

    private void saveServerInfo(Path serverPath, ServerLoader serverLoader, String version, String loader) {
        FileUtil.saveServerInfo(serverPath, serverLoader, version, loader);
    }
}
