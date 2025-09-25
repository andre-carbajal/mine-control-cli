package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.service.ExecutionService;
import net.andrecarbajal.mine_control_cli.service.server.base.AbstractServerCreator;
import net.andrecarbajal.mine_control_cli.util.ApiClientUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PaperServerCreator extends AbstractServerCreator {
    public PaperServerCreator(ConfigurationManager configurationManager, DownloadService downloadService, ExecutionService executionService) {
        super(configurationManager, downloadService, executionService);
    }

    @Override
    protected String getApiUrl() {
        return "https://api.papermc.io/v2/projects/paper/";
    }

    @Override
    protected List<String> getVersions() {
        List<String> versions = new ArrayList<>();
        JSONObject response = ApiClientUtil.getJsonObject(getApiUrl());

        if (response.has("versions")) {
            JSONArray versionArray = response.getJSONArray("versions");
            for (int i = 0; i < versionArray.length(); i++) {
                if (!versionArray.getString(i).contains("-")) {
                    versions.add(versionArray.getString(i));
                }
            }
        }
        Collections.reverse(versions);
        return versions;
    }

    @Override
    protected List<String> getLoaderVersions(String minecraftVersion) {
        return null;
    }

    private String getLatestBuild(String version) {
        JSONObject response = ApiClientUtil.getJsonObject(getApiUrl() + "versions/" + version);

        if (response.has("builds")) {
            JSONArray builds = response.getJSONArray("builds");
            if (!builds.isEmpty()) {
                return String.valueOf(builds.getInt(builds.length() - 1));
            }
        }
        throw new RuntimeException("No builds found for version " + version);
    }

    @Override
    protected String getDownloadUrl(String minecraftVersion, String loaderVersion) {
        String latestBuild = getLatestBuild(minecraftVersion);
        return getApiUrl() + "versions/" + minecraftVersion + "/builds/" + latestBuild + "/downloads/paper-" + minecraftVersion + "-" + latestBuild + ".jar";
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
        info.put("loaderType", "Paper");
        info.put("minecraftVersion", minecraftVersion);
    }

    @Override
    protected String getSuccessMessage(String serverName, String minecraftVersion) {
        return String.format("The Paper server %s has been created successfully with Minecraft version %s", serverName, minecraftVersion);
    }
}
