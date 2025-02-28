package net.andrecarbajal.mine_control_cli.util.api;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.ApplicationProperties;
import org.json.JSONArray;

import java.util.Optional;

@RequiredArgsConstructor
public class UpdateChecker {
    private final ApplicationProperties applicationProperties;

    private static final String GITHUB_API_URL = "https://api.github.com/repos/MineControlCli/mine-control-cli/tags";
    private static final String DOWNLOAD_URL = "https://github.com/MineControlCli/mine-control-cli/releases";

    public Optional<UpdateInfo> checkForUpdates() {
        JSONArray jsonArray = ApiClient.getJsonArray(GITHUB_API_URL);
        String latestVersion = jsonArray.getJSONObject(0).getString("name");
        return latestVersion != null && isNewerVersion(latestVersion) ? Optional.of(new UpdateInfo(latestVersion, DOWNLOAD_URL)) : Optional.empty();
    }

    private boolean isNewerVersion(String latestVersion) {
        String normalizedLatestVersion = latestVersion.startsWith("v") ? latestVersion.substring(1) : latestVersion;

        String[] latestParts = normalizedLatestVersion.split("\\.");
        String[] currentParts = applicationProperties.getVersion().split("\\.");

        for (int i = 0; i < Math.min(latestParts.length, currentParts.length); i++) {
            int latestPart = Integer.parseInt(latestParts[i]);
            int currentPart = Integer.parseInt(currentParts[i]);

            if (latestPart > currentPart) {
                return true;
            } else if (latestPart < currentPart) {
                return false;
            }
        }

        return latestParts.length > currentParts.length;
    }

    public record UpdateInfo(String version, String downloadUrl) {
    }
}
