package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.config.AppConfiguration;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
                for (Object version : jsonObject.getJSONArray("versions")) {
                    versions.add(version.toString());
                }
            }

            Collections.reverse(versions);

        } catch (Exception e) {
            throw new RuntimeException("Error getting Paper versions", e);
        }

        return versions;
    }

    private String getLatestBuild(String version) {
        try {
            URL url = new URL(getApiUrl() + "/versions/" + version);
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
            if (jsonObject.has("builds")) {
                JSONArray buildsArray = jsonObject.getJSONArray("builds");

                if (!buildsArray.isEmpty()) {
                    int biggestBuild = buildsArray.getInt(buildsArray.length() - 1);
                    return String.valueOf(biggestBuild); // Convert to string
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error getting Paper builds", e);
        }

        return "N/A";
    }

    @Override
    protected String getDownloadUrl(String version) {
        String latestBuild = getLatestBuild(version);
        return getApiUrl() + "/versions/" + version + "/builds/" + latestBuild + "/downloads/paper-" + version + "-" + latestBuild + ".jar";
    }
}
