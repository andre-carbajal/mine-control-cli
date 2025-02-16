package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.style.TemplateExecutor;

import java.nio.file.Path;

public abstract class AbstractUnmoddedService extends AbstractLoaderService {
    public AbstractUnmoddedService(FileDownloadService fileDownloadService) {
        super(fileDownloadService);
    }

    @Override
    public void createServer(String serverName, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        Path serverPath = prepareServerDirectory(serverName);
        try {
            String version = selectVersion(terminal, resourceLoader, templateExecutor);
            downloadServerFiles(version, serverPath);
            acceptEula(serverPath);
            logServerCreationSuccess(serverName, version);
        } catch (Exception e) {
            deleteServerDirectory(FileUtil.getMineControlCliFolder().resolve(serverName));
        }
    }

    protected abstract String getDownloadUrl(String version);

    private void downloadServerFiles(String version, Path serverPath) {
        String downloadUrl = getDownloadUrl(version);
        fileDownloadService.downloadFile(downloadUrl, serverPath, "server.jar");
    }
}
