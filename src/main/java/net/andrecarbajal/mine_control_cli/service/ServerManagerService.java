package net.andrecarbajal.mine_control_cli.service;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.util.ConversionUtil;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.SystemPathsUtil;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class ServerManagerService {
    private final ConfigurationManager configurationManager;
    private final ExecutionService executionService;

    public List<String> listServers() {
        String serversPath = configurationManager.getString("paths.servers");
        List<String> servers = FileUtil.listDirectories(serversPath);
        servers.sort(String::compareToIgnoreCase);
        return servers;
    }

    public List<String> listServersWithInfo() {
        String serversPath = configurationManager.getString("paths.servers");
        List<String> servers = FileUtil.listDirectories(serversPath);
        servers.sort(String::compareToIgnoreCase);
        return servers.stream()
                .map(serverName -> {
                    File serverDir = new File(serversPath, serverName);
                    Properties props = new Properties();
                    String version = "unknown";
                    String loaderType = "unknown";
                    String loaderVersion = null;
                    File infoFile = new File(serverDir, ".server-info.properties");
                    if (infoFile.exists()) {
                        try (FileReader reader = new FileReader(infoFile)) {
                            props.load(reader);
                            version = props.getProperty("minecraftVersion", "unknown");
                            loaderType = props.getProperty("loaderType", "unknown");
                            loaderVersion = props.getProperty("loaderVersion", null);
                        } catch (Exception ignored) {
                        }
                    }
                    long sizeBytes = FileUtil.getDirectorySize(serverDir);
                    String sizeStr = ConversionUtil.humanReadableByteCount(sizeBytes);
                    String info = serverName + " (" + version + "," + loaderType;
                    if (loaderVersion != null && !loaderVersion.isBlank()) {
                        info += ", " + loaderVersion;
                    }
                    info += ") (" + sizeStr + ")";
                    return info;
                })
                .toList();
    }

    public void deleteServer(String serverName) {
        String serversPath = configurationManager.getString("paths.servers");
        File serverDir = new File(serversPath, serverName);
        if (FileUtil.directoryNotExists(serversPath, serverName)) {
            System.out.println(TextDecorationUtil.error("Server '" + serverName + "' does not exist."));
            return;
        }
        if (FileUtil.deleteDirectoryRecursively(serverDir)) {
            System.out.println(TextDecorationUtil.success("Server '" + serverName + "' deleted successfully."));
        } else {
            System.out.println(TextDecorationUtil.error("Failed to delete server '" + serverName + "'. Please check permissions or if the server is running."));
        }
    }

    public void startServer(String serverName) {
        String serversPath = configurationManager.getString("paths.servers");
        if (FileUtil.directoryNotExists(serversPath, serverName)) {
            System.out.println(TextDecorationUtil.error("Server '" + serverName + "' does not exist."));
            return;
        }
        File serverDir = new File(serversPath, serverName);
        String loaderType = getLoaderType(serverDir);
        if (loaderType == null) loaderType = "VANILLA";
        if (loaderType.equalsIgnoreCase("NEOFORGE")) {
            if (startForgeBasedServer(serverDir, serverName, "NeoForge", "libraries/net/neoforged/neoforge")) {
                return;
            }
        }
        if (loaderType.equalsIgnoreCase("FORGE")) {
            if (startForgeBasedServer(serverDir, serverName, "Forge", "libraries/net/minecraftforge/forge")) {
                return;
            }
        }
        File serverJar = new File(serverDir, "server.jar");
        if (!serverJar.exists()) {
            System.out.println(TextDecorationUtil.error("Server jar file not found in '" + serverDir.getAbsolutePath() + "'. Please ensure the server is set up correctly."));
            return;
        }
        System.out.println(TextDecorationUtil.info("Starting server '" + serverName + "'..."));
        executionService.startServer(serverDir, serverJar.toPath());
    }

    public String getLoaderType(File serverDir) {
        File infoFile = new File(serverDir, ".server-info.properties");
        String loaderType = "VANILLA";
        if (infoFile.exists()) {
            Properties props = new Properties();
            try (FileReader reader = new FileReader(infoFile)) {
                props.load(reader);
                loaderType = props.getProperty("loaderType", "VANILLA");
            } catch (Exception e) {
                System.out.println(TextDecorationUtil.error("Could not read .server-info.properties: " + e.getMessage()));
            }
        }
        return loaderType;
    }

    private boolean startForgeBasedServer(File serverDir, String serverName, String loaderType, String librariesSubPath) {
        String osName = SystemPathsUtil.getOperatingSystemName().toLowerCase();
        String argsFileName = osName.contains("windows") ? "win_args.txt" : "unix_args.txt";
        File librariesDir = new File(serverDir, librariesSubPath);
        File[] versionDirs = librariesDir.listFiles(File::isDirectory);
        File foundArgsFile = null;
        if (versionDirs != null) {
            for (File vDir : versionDirs) {
                File candidate = new File(vDir, argsFileName);
                if (candidate.exists()) {
                    foundArgsFile = candidate;
                    break;
                }
            }
        }
        if (foundArgsFile == null) {
            System.out.println(TextDecorationUtil.error("No '" + argsFileName + "' found for " + loaderType + " in '" + librariesDir.getAbsolutePath() + "'."));
            return false;
        }
        System.out.println(TextDecorationUtil.info("Starting " + loaderType + " server '" + serverName + "'..."));
        executionService.startForgeBasedServer(serverDir, foundArgsFile.toPath());
        return true;
    }
}