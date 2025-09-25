package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.service.ExecutionService;
import net.andrecarbajal.mine_control_cli.service.server.base.AbstractServerCreator;
import net.andrecarbajal.mine_control_cli.util.ApiClientUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NeoForgeServerCreator extends AbstractServerCreator {

    public NeoForgeServerCreator(ConfigurationManager configurationManager, DownloadService downloadService, ExecutionService executionService) {
        super(configurationManager, downloadService, executionService);
    }

    @Override
    protected String getApiUrl() {
        return "https://maven.neoforged.net/api/maven/versions/releases/net%2Fneoforged%2Fneoforge";
    }

    @Override
    protected List<String> getVersions() {
        List<String> versionsList = new ArrayList<>();
        JSONObject jsonObject = ApiClientUtil.getJsonObject(getApiUrl());
        if (jsonObject.has("versions")) {
            JSONArray versions = jsonObject.getJSONArray("versions");
            for (int i = 0; i < versions.length(); i++) {
                String version = versions.getString(i);
                if (version.length() >= 4 && !version.startsWith("0.")) {
                    String mainVersion = version.substring(0, 4);
                    String formatted = "1." + mainVersion;
                    if (!versionsList.contains(formatted)) {
                        versionsList.add(formatted);
                    }
                }
            }
        }
        return versionsList.reversed();
    }

    @Override
    protected List<String> getLoaderVersions(String minecraftVersion) {
        List<String> loaderVersionsList = new ArrayList<>();
        JSONObject jsonObject = ApiClientUtil.getJsonObject(getApiUrl());
        if (jsonObject.has("versions")) {
            JSONArray versions = jsonObject.getJSONArray("versions");
            String[] parts = minecraftVersion.split("\\.");
            if (parts.length >= 2) {
                String versionPrefix = parts[1] + "." + parts[2];
                for (int i = 0; i < versions.length(); i++) {
                    String version = versions.getString(i);
                    if (version.startsWith(versionPrefix)) {
                        loaderVersionsList.add(version);
                    }
                }
            }
        }
        return loaderVersionsList.reversed();
    }

    @Override
    protected String getDownloadUrl(String minecraftVersion, String loaderVersion) {
        return "https://maven.neoforged.net/releases/net/neoforged/neoforge/" + loaderVersion + "/neoforge-" + loaderVersion + "-installer.jar";
    }

    @Override
    protected String getDownloadedFileName() {
        return "installer.jar";
    }

    @Override
    protected boolean executePostDownloadSteps() {
        return true;
    }

    @Override
    protected void populateServerInfo(Map<String, String> info, String minecraftVersion, String loaderVersion) {
        info.put("loaderType", "NeoForge");
        info.put("minecraftVersion", minecraftVersion);
        info.put("loaderVersion", loaderVersion);
    }

    @Override
    protected String getSuccessMessage(String serverName, String minecraftVersion) {
        return String.format("The NeoForge server %s has been created successfully with Minecraft version %s", serverName, minecraftVersion);
    }
}
