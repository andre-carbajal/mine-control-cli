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

        try {
            URL url = new URL(getApiUrl() + "game");
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

            JSONArray jsonArray = new JSONArray(content.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                boolean isStable = jsonObject.getBoolean("stable");

                if (isStable) {
                    String version = jsonObject.getString("version");
                    stableVersions.add(version);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error getting Fabric versions");
        }

        return stableVersions;
    }

    private String getLatestInstallerVersion() {
        try {
            URL url = new URL(getApiUrl() + "installer");
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

            JSONArray jsonArray = new JSONArray(content.toString());
            String latestStableUrl = null;

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                boolean isStable = jsonObject.getBoolean("stable");

                if (isStable) {
                    latestStableUrl = jsonObject.getString("version");
                    break;
                }
            }

            return latestStableUrl;

        } catch (Exception e) {
            throw new RuntimeException("Error getting Fabric installer version");
        }
    }

    @Override
    protected List<String> getLoaderVersions() {
        List<String> versions = new ArrayList<>();

        try {
            URL url = new URL(getApiUrl() + "loader");
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

            JSONArray jsonArray = new JSONArray(content.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String version = jsonObject.getString("version");
                versions.add(version);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error getting Fabric loader versions");
        }

        return versions;
    }

    @Override
    protected String getDownloadUrl(String version, String loaderVersion) {
        return getApiUrl() + "loader/" + version + "/" + loaderVersion + "/" + getLatestInstallerVersion() + "/server/jar";
    }
}
