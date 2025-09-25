package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.model.LoaderType;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.service.ExecutionService;
import net.andrecarbajal.mine_control_cli.service.server.base.AbstractServerCreator;
import net.andrecarbajal.mine_control_cli.util.ApiClientUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MojangServerCreator extends AbstractServerCreator {
    private final String versionType;
    private final LoaderType loaderType;

    public MojangServerCreator(LoaderType loaderType, ConfigurationManager configurationManager, DownloadService downloadService, ExecutionService executionService) {
        super(configurationManager, downloadService, executionService);
        this.loaderType = loaderType;
        switch (loaderType) {
            case VANILLA -> this.versionType = "release";
            case SNAPSHOT -> this.versionType = "snapshot";
            default -> throw new IllegalArgumentException("Invalid loader type for MojangServerCreator: " + loaderType);
        }
    }

    @Override
    protected String getApiUrl() {
        return "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    }

    @Override
    protected List<String> getVersions() {
        List<String> versionsList = new ArrayList<>();
        JSONObject manifest = ApiClientUtil.getJsonObject(getApiUrl());
        if (manifest.has("versions")) {
            JSONArray versions = manifest.getJSONArray("versions");
            for (int i = 0; i < versions.length(); i++) {
                JSONObject version = versions.getJSONObject(i);
                if (version.getString("type").equals(versionType)) {
                    versionsList.add(version.getString("id"));
                }
            }
        }
        return versionsList;
    }

    @Override
    protected List<String> getLoaderVersions(String minecraftVersion) {
        return null;
    }

    @Override
    protected String getDownloadUrl(String minecraftVersion, String loaderVersion) {
        JSONObject manifest = ApiClientUtil.getJsonObject(getApiUrl());
        JSONArray versions = manifest.getJSONArray("versions");
        for (int i = 0; i < versions.length(); i++) {
            JSONObject versionEntry = versions.getJSONObject(i);
            if (versionEntry.getString("id").equals(minecraftVersion)) {
                JSONObject versionDetails = ApiClientUtil.getJsonObject(versionEntry.getString("url"));
                return versionDetails.getJSONObject("downloads")
                        .getJSONObject("server")
                        .getString("url");
            }
        }
        throw new RuntimeException("Version " + minecraftVersion + " not found");
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
        info.put("loaderType", loaderType.getDisplayName());
        info.put("minecraftVersion", minecraftVersion);
    }

    @Override
    protected String getSuccessMessage(String serverName, String minecraftVersion) {
        return String.format("The %s server %s has been created successfully with Minecraft version %s", loaderType.getDisplayName(), serverName, minecraftVersion);
    }
}
