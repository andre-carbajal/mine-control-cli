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

public class FabricServerCreator extends AbstractServerCreator {

    public FabricServerCreator(ConfigurationManager configurationManager, DownloadService downloadService, ExecutionService executionService) {
        super(configurationManager, downloadService, executionService);
    }

    @Override
    protected String getApiUrl() {
        return "https://meta.fabricmc.net/v2/versions/";
    }

    @Override
    protected List<String> getVersions() {
        List<String> stableVersions = new ArrayList<>();
        JSONArray jsonArray = ApiClientUtil.getJsonArray(getApiUrl() + "game");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.getBoolean("stable")) {
                stableVersions.add(jsonObject.getString("version"));
            }
        }
        return stableVersions;
    }

    @Override
    protected List<String> getLoaderVersions(String minecraftVersion) {
        List<String> versions = new ArrayList<>();
        JSONArray jsonArray = ApiClientUtil.getJsonArray(getApiUrl() + "loader");

        for (int i = 0; i < jsonArray.length(); i++) {
            versions.add(jsonArray.getJSONObject(i).getString("version"));
        }
        return versions;
    }

    private String getLatestInstallerVersion() {
        JSONArray jsonArray = ApiClientUtil.getJsonArray(getApiUrl() + "installer");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.getBoolean("stable")) {
                return jsonObject.getString("version");
            }
        }
        throw new RuntimeException("No stable installer version found");
    }

    @Override
    protected String getDownloadUrl(String minecraftVersion, String loaderVersion) {
        return getApiUrl() + "loader/" + minecraftVersion + "/" + loaderVersion + "/" + getLatestInstallerVersion() + "/server/jar";
    }

    @Override
    protected String getDownloadedFileName() {
        return "server.jar";
    }

    @Override
    protected boolean executePostDownloadSteps() {
        return false;
    }

    @Override
    protected void populateServerInfo(Map<String, String> info, String minecraftVersion, String loaderVersion) {
        info.put("loaderType", "Fabric");
        info.put("minecraftVersion", minecraftVersion);
        info.put("loaderVersion", loaderVersion);
    }

    @Override
    protected String getSuccessMessage(String serverName, String minecraftVersion) {
        return String.format("The Fabric server %s has been created successfully with Minecraft version %s", serverName, minecraftVersion);
    }
}
