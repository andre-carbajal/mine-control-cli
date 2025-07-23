package net.andrecarbajal.mine_control_cli.service;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
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
    private final ServerProcessService serverProcessService;

    public List<String> listServers() {
        String serversPath = configurationManager.getString("paths.servers");
        return FileUtil.listDirectories(serversPath);
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
        File infoFile = new File(serverDir, ".server-info.properties");
        String loaderType = "VANILLA";
        if (infoFile.exists()) {
            Properties props = new Properties();
            try (FileReader reader = new FileReader(infoFile)) {
                props.load(reader);
                loaderType = props.getProperty("loaderType", "VANILLA");
            } catch (Exception e) {
                System.out.println(TextDecorationUtil.error("Could not read .server-info.properties: " + e.getMessage()));
                return;
            }
        }
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
        serverProcessService.startServer(serverDir, serverJar.toPath());
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
        serverProcessService.startForgeBasedServer(serverDir, foundArgsFile.toPath());
        return true;
    }
}