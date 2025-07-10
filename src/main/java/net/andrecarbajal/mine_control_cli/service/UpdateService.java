package net.andrecarbajal.mine_control_cli.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.andrecarbajal.mine_control_cli.config.properties.SpringApplicationProperties;
import net.andrecarbajal.mine_control_cli.util.ApiClientUtil;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.json.JSONArray;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateService {
    private final SpringApplicationProperties springApplicationProperties;

    private static final String GITHUB_API_URL = "https://api.github.com/repos/MineControlCli/mine-control-cli/tags";
    private static final String DOWNLOAD_URL = "https://github.com/MineControlCli/mine-control-cli/releases";

    public void showUpdateMessage(boolean showLatestVersion) {
        Optional<UpdateInfo> updateInfoOpt = checkForUpdates();
        if (updateInfoOpt.isPresent()) {
            UpdateInfo updateInfo = updateInfoOpt.get();
            System.out.println(TextDecorationUtil.info("A new version is available: ") + TextDecorationUtil.cyan(updateInfo.version()));
            System.out.println(TextDecorationUtil.info("Download it at: ") + TextDecorationUtil.green(updateInfo.downloadUrl()));
        } else if (showLatestVersion) {
            System.out.println(TextDecorationUtil.success("You are using the latest version: ") + TextDecorationUtil.cyan(springApplicationProperties.getVersion()));
        }
    }

    private Optional<UpdateInfo> checkForUpdates() {
        JSONArray jsonArray = ApiClientUtil.getJsonArray(GITHUB_API_URL);
        String latestVersion = jsonArray.getJSONObject(0).getString("name");
        return latestVersion != null && isNewerVersion(latestVersion) ? Optional.of(new UpdateInfo(latestVersion, DOWNLOAD_URL)) : Optional.empty();
    }

    private boolean isNewerVersion(String latestVersion) {
        String normalizedLatestVersion = latestVersion.startsWith("v") ? latestVersion.substring(1) : latestVersion;

        String[] latestParts = normalizedLatestVersion.split("\\.");
        String[] currentParts = springApplicationProperties.getVersion().split("\\.");

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
