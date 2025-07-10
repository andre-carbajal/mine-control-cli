package net.andrecarbajal.mine_control_cli.service.server.paper;

import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.model.LoaderType;
import net.andrecarbajal.mine_control_cli.service.DownloadService;
import net.andrecarbajal.mine_control_cli.service.server.base.AbstractUnmoddedServerCreator;
import net.andrecarbajal.mine_control_cli.util.ApiClientUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaperServerCreator extends AbstractUnmoddedServerCreator {
    public PaperServerCreator(LoaderType loaderType, ConfigurationManager configurationManager, DownloadService downloadService) {
        super(loaderType, configurationManager, downloadService);
    }

    @Override
    protected String getApiUrl() {
        return "https://api.papermc.io/v2/projects/paper";
    }

    @Override
    protected List<String> getVersions() {
        List<String> versions = new ArrayList<>();
        JSONObject response = ApiClientUtil.getJsonObject(getApiUrl());

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
        JSONObject response = ApiClientUtil.getJsonObject(getApiUrl() + "/versions/" + version);

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
