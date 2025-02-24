package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.config.MineControlConfig;
import net.andrecarbajal.mine_control_cli.model.mojang.MojangServerResponse;
import net.andrecarbajal.mine_control_cli.model.mojang.MojangVersionsResponse;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;

import java.util.List;

public abstract class MojangService extends AbstractUnmoddedService {
    public abstract String type();

    public MojangService(MineControlConfig mineControlConfig, FileUtil fileUtil, FileDownloadService fileDownloadService) {
        super(mineControlConfig, fileUtil, fileDownloadService);
    }

    @Override
    public String getApiUrl() {
        return "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    }

    @Override
    protected List<String> getVersions() {
        MojangVersionsResponse response = restTemplate.getForObject(getApiUrl(), MojangVersionsResponse.class);

        if (response != null && response.getVersions() != null) {
            return response.getVersions().stream()
                    .filter(version -> version.getType().equals(type()))
                    .map(MojangVersionsResponse.Version::getId).toList();
        } else {
            throw new RuntimeException("Failed to retrieve versions from Mojang API");
        }
    }

    @Override
    protected String getDownloadUrl(String version) {
        MojangVersionsResponse versionsResponse = restTemplate.getForObject(getApiUrl(), MojangVersionsResponse.class);
        if (versionsResponse != null && versionsResponse.getVersions() != null) {
            String url = versionsResponse.getVersions().stream()
                    .filter(vanillaVersion -> vanillaVersion.getId().equals(version))
                    .map(MojangVersionsResponse.Version::getUrl)
                    .findFirst().orElse(null);
            if (url != null) {
                MojangServerResponse serverResponse = restTemplate.getForObject(url, MojangServerResponse.class);
                if (serverResponse != null && serverResponse.getDownloads() != null && serverResponse.getDownloads().getServer() != null) {
                    return serverResponse.getDownloads().getServer().getUrl();
                } else {
                    throw new RuntimeException("Unable to retrieve server download URL from Mojang API");
                }
            }
            throw new RuntimeException("Unable to retrieve server download URL from Mojang API");

        } else {
            throw new RuntimeException("Failed to retrieve server download URL from Mojang API");
        }
    }
}
