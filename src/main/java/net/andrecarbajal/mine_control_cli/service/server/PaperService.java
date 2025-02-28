package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.config.AppConfiguration;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.io.FileUtil;
import net.andrecarbajal.mine_control_cli.util.api.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PaperService extends AbstractUnmoddedService {
    public PaperService(AppConfiguration appConfiguration, FileUtil fileUtil, FileDownloadService fileDownloadService) {
        super(appConfiguration, fileUtil, fileDownloadService);
    }

    @Override
    protected String getApiUrl() {
        return "https://api.papermc.io/v2/projects/paper";
    }

    @Override
    protected List<String> getVersions() {
        List<String> versions = new ArrayList<>();
        JSONObject response = ApiClient.getJsonObject(getApiUrl());

        if (response.has("versions")) {
            JSONArray versionArray = response.getJSONArray("versions");
            for (int i = 0; i < versionArray.length(); i++) {
                versions.add(versionArray.getString(i));
            }
        }
        Collections.reverse(versions);
        return versions;
    }

    private String getLatestBuild(String version) {
        JSONObject response = ApiClient.getJsonObject(getApiUrl() + "/versions/" + version);

        if (response.has("builds")) {
            JSONArray builds = response.getJSONArray("builds");
            if (!builds.isEmpty()) {
                return String.valueOf(builds.getInt(builds.length() - 1));
            }
        }
        throw new RuntimeException("No builds found for version " + version);
    }

    @Override
    protected String getDownloadUrl(String version) {
        String latestBuild = getLatestBuild(version);
        return getApiUrl() + "/versions/" + version + "/builds/" + latestBuild + "/downloads/paper-" + version + "-" + latestBuild + ".jar";
    }
}
