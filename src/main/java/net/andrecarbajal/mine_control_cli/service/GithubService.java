package net.andrecarbajal.mine_control_cli.service;

import net.andrecarbajal.mine_control_cli.util.ApiClientUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.andrecarbajal.mine_control_cli.model.GithubAsset;

@Service
public class GithubService {
    private final String username;
    private final String repository;

    GithubService(String username, String repository) {
        this.username = username;
        this.repository = repository;
    }

    public Optional<String> getLatestReleaseTag() {
        String url = String.format("https://api.github.com/repos/%s/%s/tags", this.username, this.repository);
        JSONArray jsonArray = ApiClientUtil.getJsonArray(url);
        if (jsonArray == null || jsonArray.isEmpty() || !jsonArray.getJSONObject(0).has("name")) {
            return Optional.empty();
        }
        String latestTag = jsonArray.getJSONObject(0).getString("name");
        return Optional.ofNullable(latestTag);
    }

    public Optional<List<GithubAsset>> getLatestReleaseAssetList() {
        String url = String.format("https://api.github.com/repos/%s/%s/releases/latest", this.username, this.repository);
        JSONObject releaseJson = ApiClientUtil.getJsonObject(url);
        if (releaseJson == null || releaseJson.isEmpty() || !releaseJson.has("assets")) {
            return Optional.empty();
        }
        JSONArray assetsArray = releaseJson.getJSONArray("assets");
        List<GithubAsset> assetList = new ArrayList<>();
        for (int i = 0; i < assetsArray.length(); i++) {
            JSONObject assetObj = assetsArray.getJSONObject(i);
            String name = assetObj.optString("name", "");
            String urlDownload = assetObj.optString("browser_download_url", "");
            assetList.add(new GithubAsset(name, urlDownload));
        }
        return Optional.of(assetList);
    }

    public boolean isCurrentVersionLatest(String currentVersion) {
        Optional<String> latestVersionOpt = getLatestReleaseTag();
        if (latestVersionOpt.isEmpty()) {
            return true;
        }

        String latestVersion = latestVersionOpt.get();
        String normalizedLatestVersion = latestVersion.startsWith("v") ? latestVersion.substring(1) : latestVersion;
        String[] latestParts = normalizedLatestVersion.split("\\.");
        String normalizedCurrentVersion = currentVersion.startsWith("v") ? currentVersion.substring(1) : currentVersion;
        String[] currentParts = normalizedCurrentVersion.split("\\.");
        for (int i = 0; i < Math.min(latestParts.length, currentParts.length); i++) {
            int latestPart = Integer.parseInt(latestParts[i]);
            int currentPart = Integer.parseInt(currentParts[i]);
            if (latestPart > currentPart) {
                return false;
            } else if (latestPart < currentPart) {
                return true;
            }
        }
        return currentParts.length >= latestParts.length;
    }
}
