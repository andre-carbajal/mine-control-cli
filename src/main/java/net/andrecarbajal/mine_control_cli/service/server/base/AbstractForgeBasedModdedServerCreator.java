package net.andrecarbajal.mine_control_cli.service.server.base;

import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.model.LoaderType;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.service.ServerProcessService;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.style.TemplateExecutor;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractForgeBasedModdedServerCreator extends AbstractServerCreator {
    private final LoaderType loaderType;
    private final DownloadService downloadService;
    private final ServerProcessService serverProcessService;

    public AbstractForgeBasedModdedServerCreator(LoaderType loaderType, ConfigurationManager configurationManager, DownloadService downloadService, ServerProcessService serverProcessService) {
        super(configurationManager);
        this.loaderType = loaderType;
        this.downloadService = downloadService;
        this.serverProcessService = serverProcessService;
    }

    @Override
    public void createServer(String serverName, String loaderVersion, String minecraftVersion, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        if (createServerDirectory(serverName)) {
            try {
                minecraftVersion = selectVersion(getVersions(), "Select a Minecraft version for the server:", minecraftVersion, terminal, resourceLoader, templateExecutor);
                loaderVersion = selectVersion(getLoaderVersions(minecraftVersion), "Select a loader version for the server:", loaderVersion, terminal, resourceLoader, templateExecutor);
                String downloadUrl = getDownloadUrl(minecraftVersion, loaderVersion);
                downloadServerFile(downloadUrl, serverName, "installer.jar", downloadService);
                executeServerInstaller(serverName);
                Map<String, String> info = new HashMap<>();
                info.put("loaderType", loaderType.name());
                info.put("minecraftVersion", minecraftVersion);
                info.put("loaderVersion", loaderVersion);
                writeServerInfo(serverName, info);
                System.out.println(TextDecorationUtil.success("The server " + loaderType.getDisplayName() + " " + serverName + " has been created successfully with Minecraft version " + minecraftVersion));
            } catch (Exception e) {
                System.out.println(TextDecorationUtil.error("Error creating server: " + e.getMessage()));
                deleteServerDirectory(serverName);
            }
        }
    }

    protected abstract List<String> getLoaderVersions(String minecraftVersion);

    protected abstract String getDownloadUrl(String version, String loaderVersion);

    private void executeServerInstaller(String serverName) {
        Path serverPath = Path.of(configurationManager.getString("paths.servers"), serverName);
        Path jarFilePath = serverPath.resolve("installer.jar");
        if (!jarFilePath.toFile().exists()) {
            throw new RuntimeException("Installer jar not found at " + jarFilePath);
        }
        serverProcessService.startInstaller(serverPath.toFile(), jarFilePath);
    }
}
