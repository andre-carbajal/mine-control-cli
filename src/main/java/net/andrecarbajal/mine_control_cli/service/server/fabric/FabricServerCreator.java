package net.andrecarbajal.mine_control_cli.service.server.fabric;

import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.service.server.base.AbstractFabricModdedServerCreator;
import net.andrecarbajal.mine_control_cli.util.ApiClientUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FabricServerCreator extends AbstractFabricModdedServerCreator {
    public FabricServerCreator(ConfigurationManager configurationManager, DownloadService downloadService) {
        super(configurationManager, downloadService);
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
    protected List<String> getLoaderVersions() {
        List<String> versions = new ArrayList<>();
        JSONArray jsonArray = ApiClientUtil.getJsonArray(getApiUrl() + "loader");

        for (int i = 0; i < jsonArray.length(); i++) {
            versions.add(jsonArray.getJSONObject(i).getString("version"));
        }
        return versions;
    }

    @Override
    protected String getDownloadUrl(String version, String loaderVersion) {
        return getApiUrl() + "loader/" + version + "/" + loaderVersion + "/" + getLatestInstallerVersion() + "/server/jar";
    }
}
