package net.andrecarbajal.mine_control_cli.service.server.base;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.util.ComponentUtil;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.style.TemplateExecutor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractServerCreator implements ServerCreator {
    protected final ConfigurationManager configurationManager;

    protected abstract String getApiUrl();
    protected abstract List<String> getVersions();

    @Override
    public abstract void createServer(String serverName, String loaderVersion, String minecraftVersion, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor);

    protected boolean createServerDirectory(String serverName) {
        String serversBasePath = configurationManager.getString("paths.servers");
        Path serverDir = Path.of(serversBasePath, serverName);
        if (Files.exists(serverDir)) {
            System.out.println("Server directory already exists: " + serverDir + ". Cancelling creation.");
            return false;
        }
        try {
            FileUtil.createDirectoryIfNotExists(serverDir);
            autoAcceptEulaIfConfigured(serverDir);
            return true;
        } catch (Exception e) {
            System.out.println("Error creating server directory: " + serverDir + ", error: " + e.getMessage());
            return false;
        }
    }

    private void autoAcceptEulaIfConfigured(Path serverDir) {
        boolean autoAccept = configurationManager.getBoolean("eula.auto-accept");
        if (autoAccept) {
            Path eulaFile = serverDir.resolve("eula.txt");
            try {
                Files.writeString(eulaFile, "eula=true\n");
                System.out.println(TextDecorationUtil.success("EULA accepted automatically for the server"));
            } catch (Exception e) {
                System.out.println(TextDecorationUtil.error("Could not write eula.txt automatically: " + e.getMessage()));
            }
        }
    }

    protected void deleteServerDirectory(String serverName) {
        String serversBasePath = configurationManager.getString("paths.servers");
        Path serverDir = Path.of(serversBasePath, serverName);
        try {
            FileUtil.deleteDirectoryRecursively(serverDir.toFile());
        } catch (Exception e) {
            System.out.println("Error deleting server directory: " + serverDir + ", error: " + e.getMessage());
        }
    }

    protected void writeServerInfo(String serverName, Map<String, String> info) {
        Path serverPath = Path.of(configurationManager.getString("paths.servers"), serverName);
        FileUtil.writeServerInfo(serverPath, info);
    }

    protected void downloadServerFile(String downloadUrl, String serverName, String fileName, DownloadService downloadService) {
        Path serverPath = Path.of(configurationManager.getString("paths.servers"), serverName);
        downloadService.downloadFile(downloadUrl, serverPath, fileName);
    }

    protected String selectVersion(List<String> versions, String prompt, String currentVersion, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        if (currentVersion == null || !versions.contains(currentVersion)) {
            return ComponentUtil.selectString(versions, prompt, terminal, resourceLoader, templateExecutor);
        }
        return currentVersion;
    }
}
