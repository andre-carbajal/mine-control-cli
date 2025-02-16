package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.model.paper.PaperBuildResponse;
import net.andrecarbajal.mine_control_cli.model.paper.PaperResponse;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PaperService extends AbstractUnmoddedService {
    public PaperService(FileDownloadService fileDownloadService) {
        super(fileDownloadService);
    }

    @Override
    protected String getApiUrl() {
        return "https://api.papermc.io/v2/projects/paper";
    }

    @Override
    protected List<String> getVersions() {
        PaperResponse response = restTemplate.getForObject(getApiUrl(), PaperResponse.class);

        if (response != null && response.getVersions() != null) {
            var versions = response.getVersions();
            Collections.reverse(versions);
            return versions;
        }
        throw new RuntimeException("Error getting Paper versions");
    }

    private String getLatestBuild(String version) {
        PaperBuildResponse response = restTemplate.getForObject(getApiUrl() + "/versions/" + version, PaperBuildResponse.class);

        if (response != null && response.getBuilds() != null) {
            return String.valueOf(response.getBuilds()[response.getBuilds().length - 1]);
        }
        throw new RuntimeException("Error getting Paper builds");
    }

    @Override
    protected String getDownloadUrl(String version) {
        String latestBuild = getLatestBuild(version);
        return getApiUrl() + "/versions/" + version + "/builds/" + latestBuild + "/downloads/paper-" + version + "-" + latestBuild + ".jar";
    }
}
