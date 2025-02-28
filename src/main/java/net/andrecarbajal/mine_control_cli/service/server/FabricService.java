package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.config.AppConfiguration;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FabricService extends AbstractModdedLoaderService {
    public FabricService(AppConfiguration appConfiguration, FileUtil fileUtil, FileDownloadService fileDownloadService) {
        super(appConfiguration, fileUtil, fileDownloadService);
    }

    @Override
    protected String getApiUrl() {
        return "https://meta.fabricmc.net/v2/versions/";
    }

    @Override
    protected List<String> getVersions() {
        List<String> stableVersions = new ArrayList<>();
        JSONArray jsonArray = ApiClient.getJsonArray(getApiUrl() + "game");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.getBoolean("stable")) {
                stableVersions.add(jsonObject.getString("version"));
            }
        }
        return stableVersions;
    }

    private String getLatestInstallerVersion() {
        JSONArray jsonArray = ApiClient.getJsonArray(getApiUrl() + "installer");

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
        JSONArray jsonArray = ApiClient.getJsonArray(getApiUrl() + "loader");

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
