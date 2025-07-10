package net.andrecarbajal.mine_control_cli.service.server.mojang;

import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.model.LoaderType;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.service.server.base.AbstractUnmoddedServerCreator;
import net.andrecarbajal.mine_control_cli.util.ApiClientUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class MojangServerCreator extends AbstractUnmoddedServerCreator {
    public MojangServerCreator(LoaderType loaderType, ConfigurationManager configurationManager, DownloadService downloadService) {
        super(loaderType, configurationManager, downloadService);
    }

    protected String getApiUrl() {
        return "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    }

    protected abstract String getVersionType();

    @Override
    protected List<String> getVersions() {
        List<String> versionsList = new ArrayList<>();
        JSONObject manifest = ApiClientUtil.getJsonObject(getApiUrl());
        if (manifest.has("versions")) {
            JSONArray versions = manifest.getJSONArray("versions");
            for (int i = 0; i < versions.length(); i++) {
                JSONObject version = versions.getJSONObject(i);
                if (version.getString("type").equals(getVersionType())) {
                    versionsList.add(version.getString("id"));
                }
            }
        }
        return versionsList;
    }

    @Override
    protected String getDownloadUrl(String version) {
        JSONObject manifest = ApiClientUtil.getJsonObject(getApiUrl());
        JSONArray versions = manifest.getJSONArray("versions");
        for (int i = 0; i < versions.length(); i++) {
            JSONObject versionEntry = versions.getJSONObject(i);
            if (versionEntry.getString("id").equals(version)) {
                JSONObject versionDetails = ApiClientUtil.getJsonObject(versionEntry.getString("url"));
                return versionDetails.getJSONObject("downloads")
                        .getJSONObject("server")
                        .getString("url");
            }
        }
        throw new RuntimeException("Version " + version + " not found");
    }
}
