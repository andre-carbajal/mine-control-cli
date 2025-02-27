package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.config.AppConfiguration;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

        try {
            URL url = new URL(getApiUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();

            JSONObject jsonObject = new JSONObject(content.toString());
            if (jsonObject.has("versions")) {
                JSONArray versionsArray = jsonObject.getJSONArray("versions");

                for (int i = 0; i < versionsArray.length(); i++) {
                    JSONObject versionObject = versionsArray.getJSONObject(i);
                    String type = versionObject.getString("type");

                    if (type().equals(type)) {
                        String versionId = versionObject.getString("id");
                        releaseVersions.add(versionId);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve versions from Mojang API", e);
        }

        return releaseVersions;
    }

    @Override
    protected String getDownloadUrl(String version) {
        try {
            URL manifestUrl = new URL(getApiUrl());
            HttpURLConnection manifestConnection = (HttpURLConnection) manifestUrl.openConnection();
            manifestConnection.setRequestMethod("GET");

            BufferedReader manifestReader = new BufferedReader(new InputStreamReader(manifestConnection.getInputStream()));
            StringBuilder manifestContent = new StringBuilder();
            String line;
            while ((line = manifestReader.readLine()) != null) {
                manifestContent.append(line);
            }
            manifestReader.close();
            manifestConnection.disconnect();

            JSONObject manifestJson = new JSONObject(manifestContent.toString());
            JSONArray versions = manifestJson.getJSONArray("versions");
            String versionUrl = null;

            for (int i = 0; i < versions.length(); i++) {
                JSONObject versionEntry = versions.getJSONObject(i);
                if (versionEntry.getString("id").equals(version)) {
                    versionUrl = versionEntry.getString("url");
                    break;
                }
            }

            if (versionUrl == null) return null;

            URL versionSpecificUrl = new URL(versionUrl);
            HttpURLConnection versionConnection = (HttpURLConnection) versionSpecificUrl.openConnection();
            versionConnection.setRequestMethod("GET");

            BufferedReader versionReader = new BufferedReader(new InputStreamReader(versionConnection.getInputStream()));
            StringBuilder versionContent = new StringBuilder();
            while ((line = versionReader.readLine()) != null) {
                versionContent.append(line);
            }
            versionReader.close();
            versionConnection.disconnect();

            JSONObject versionJson = new JSONObject(versionContent.toString());
            return versionJson.getJSONObject("downloads")
                    .getJSONObject("server")
                    .getString("url");

        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve download URL from Mojang API", e);
        }
    }
}
