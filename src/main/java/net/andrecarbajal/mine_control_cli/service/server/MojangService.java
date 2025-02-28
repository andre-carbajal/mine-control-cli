package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.config.AppConfiguration;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class MojangService extends AbstractUnmoddedService {
    public abstract String type();

    public MojangService(AppConfiguration appConfiguration, FileUtil fileUtil, FileDownloadService fileDownloadService) {
        super(appConfiguration, fileUtil, fileDownloadService);
    }

    @Override
    public String getApiUrl() {
        return "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    }

    @Override
    protected List<String> getVersions() {
        List<String> releaseVersions = new ArrayList<>();
        JSONObject manifest = ApiClient.getJsonObject(getApiUrl());

        if (manifest.has("versions")) {
            JSONArray versions = manifest.getJSONArray("versions");

            for (int i = 0; i < versions.length(); i++) {
                JSONObject version = versions.getJSONObject(i);
                if (version.getString("type").equals(type())) {
                    releaseVersions.add(version.getString("id"));
                }
            }
        }
        return releaseVersions;
    }

    @Override
    protected String getDownloadUrl(String version) {
        JSONObject manifest = ApiClient.getJsonObject(getApiUrl());
        JSONArray versions = manifest.getJSONArray("versions");

        for (int i = 0; i < versions.length(); i++) {
            JSONObject versionEntry = versions.getJSONObject(i);
            if (versionEntry.getString("id").equals(version)) {
                JSONObject versionDetails = ApiClient.getJsonObject(versionEntry.getString("url"));
                return versionDetails.getJSONObject("downloads")
                        .getJSONObject("server")
                        .getString("url");
            }
        }
        throw new RuntimeException("Version " + version + " not found");
    }
}
