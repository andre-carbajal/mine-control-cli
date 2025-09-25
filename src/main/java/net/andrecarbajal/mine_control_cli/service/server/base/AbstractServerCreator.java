package net.andrecarbajal.mine_control_cli.service.server.base;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.service.ExecutionService;
import net.andrecarbajal.mine_control_cli.util.ComponentUtil;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.style.TemplateExecutor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractServerCreator implements IServerCreator {
    private final ConfigurationManager configurationManager;
    private final DownloadService downloadService;
    private final ExecutionService executionService;

    @Override
    public final void createServer(String serverName, String loaderVersion, String minecraftVersion, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        if (!createServerDirectory(serverName)) return;

        try {
            String finalMinecraftVersion = selectVersion(getVersions(), minecraftVersion, "Select a Minecraft version for the server:", terminal, resourceLoader, templateExecutor);
            var loaderVersions = getLoaderVersions(finalMinecraftVersion);
            String finalLoaderVersion = null;
            if (loaderVersions != null && !loaderVersions.isEmpty()) {
                finalLoaderVersion = selectVersion(loaderVersions, loaderVersion, "Select a Loader Version for the server", terminal, resourceLoader, templateExecutor);
                if (finalLoaderVersion == null) {
                    System.out.println(TextDecorationUtil.error("No loader version selected. Cancelling server creation."));
                    deleteServerDirectory(serverName);
                    return;
                }
            }

            String downloadUrl = getDownloadUrl(finalMinecraftVersion, finalLoaderVersion);
            String downloadedFileName = getDownloadedFileName();
            downloadServerFile(downloadUrl, serverName, downloadedFileName, downloadService);

            if (executePostDownloadSteps()) {
                int installerExitCode = executeServerInstaller(serverName);
                if (installerExitCode != 0) {
                    throw new RuntimeException("Server installer failed with exit code: " + installerExitCode);
                }
            }

            Map<String, String> info = new HashMap<>();
            populateServerInfo(info, finalMinecraftVersion, finalLoaderVersion);
            writeServerInfo(serverName, info);

            System.out.println(TextDecorationUtil.success(getSuccessMessage(serverName, finalMinecraftVersion)));
        } catch (Exception e) {
            System.out.println(TextDecorationUtil.error("Error creating server: " + e.getMessage()));
            deleteServerDirectory(serverName);
        }
    }

    protected abstract String getApiUrl();

    protected abstract List<String> getVersions();

    protected abstract List<String> getLoaderVersions(String minecraftVersion);

    protected abstract String getDownloadUrl(String minecraftVersion, String loaderVersion);

    protected abstract String getDownloadedFileName();

    protected abstract boolean executePostDownloadSteps();

    protected abstract void populateServerInfo(Map<String, String> info, String minecraftVersion, String loaderVersion);

    protected abstract String getSuccessMessage(String serverName, String minecraftVersion);


    private boolean createServerDirectory(String serverName) {
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

    private void writeServerInfo(String serverName, Map<String, String> info) {
        Path serverPath = Path.of(configurationManager.getString("paths.servers"), serverName);
        FileUtil.writeServerInfo(serverPath, info);
    }

    private void downloadServerFile(String downloadUrl, String serverName, String fileName, DownloadService downloadService) {
        Path serverPath = Path.of(configurationManager.getString("paths.servers"), serverName);
        downloadService.downloadFile(downloadUrl, serverPath, fileName);
    }

    private String selectVersion(List<String> versions, String currentVersion, String prompt, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        if (versions == null) return null;
        if (currentVersion == null || !versions.contains(currentVersion)) {
            return ComponentUtil.selectString(versions, prompt, terminal, resourceLoader, templateExecutor);
        }
        return currentVersion;
    }

    private void deleteServerDirectory(String serverName) {
        String serversBasePath = configurationManager.getString("paths.servers");
        Path serverDir = Path.of(serversBasePath, serverName);
        try {
            FileUtil.deleteDirectoryRecursively(serverDir.toFile());
        } catch (Exception e) {
            System.out.println("Error deleting server directory: " + serverDir + ", error: " + e.getMessage());
        }
    }

    private int executeServerInstaller(String serverName) {
        Path serverPath = Path.of(configurationManager.getString("paths.servers"), serverName);
        Path jarFilePath = serverPath.resolve(getDownloadedFileName());
        if (!jarFilePath.toFile().exists()) {
            throw new RuntimeException("Installer jar not found at " + jarFilePath);
        }
        return executionService.startInstaller(serverPath.toFile(), jarFilePath);
    }
}
