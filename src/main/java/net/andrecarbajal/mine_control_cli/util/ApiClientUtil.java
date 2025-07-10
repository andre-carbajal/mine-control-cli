package net.andrecarbajal.mine_control_cli.util;

import lombok.experimental.UtilityClass;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@UtilityClass
public class ApiClientUtil {
    public JSONArray getJsonArray(String urlString) {
        String content = fetchUrlContent(urlString);
        return new JSONArray(content);
    }

    public JSONObject getJsonObject(String urlString) {
        String content = fetchUrlContent(urlString);
        return new JSONObject(content);
    }

    private String fetchUrlContent(String urlString) {
        try {
            URL url = URI.create(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
            }
            reader.close();
            connection.disconnect();

            return content.toString();
        } catch (Exception e) {
            throw new RuntimeException("API request failed for URL: " + urlString, e);
        }
    }
}
