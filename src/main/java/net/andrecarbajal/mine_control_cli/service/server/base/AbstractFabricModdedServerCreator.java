package net.andrecarbajal.mine_control_cli.service.server.base;

import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.style.TemplateExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractFabricModdedServerCreator extends AbstractServerCreator {
    private final DownloadService downloadService;

    public AbstractFabricModdedServerCreator(ConfigurationManager configurationManager, DownloadService downloadService) {
        super(configurationManager);
        this.downloadService = downloadService;
    }

    @Override
    public void createServer(String serverName, String loaderVersion, String minecraftVersion, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        if (createServerDirectory(serverName)) {
            try {
                minecraftVersion = selectVersion(getVersions(), "Select a Minecraft version for the server:", minecraftVersion, terminal, resourceLoader, templateExecutor);
                loaderVersion = selectVersion(getLoaderVersions(), "Select a loader version for the server:", loaderVersion, terminal, resourceLoader, templateExecutor);
                String downloadUrl = getDownloadUrl(minecraftVersion, loaderVersion);
                downloadServerFile(downloadUrl, serverName, "server.jar", downloadService);
                Map<String, String> info = new HashMap<>();
                info.put("loaderType", "FABRIC");
                info.put("minecraftVersion", minecraftVersion);
                info.put("loaderVersion", loaderVersion);
                writeServerInfo(serverName, info);
                System.out.println(TextDecorationUtil.success("The server Fabric " + serverName + " has been created successfully with Minecraft version " + minecraftVersion));
            } catch (Exception e) {
                System.out.println(TextDecorationUtil.error("Error creating server: " + e.getMessage()));
                deleteServerDirectory(serverName);
            }
        }
    }

    protected abstract List<String> getLoaderVersions();

    protected abstract String getDownloadUrl(String version, String loaderVersion);
}
