package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.config.AppConfiguration;
import net.andrecarbajal.mine_control_cli.model.fabric.FabricInstallerResponse;
import net.andrecarbajal.mine_control_cli.model.fabric.FabricLoaderResponse;
import net.andrecarbajal.mine_control_cli.model.fabric.FabricVersionResponse;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
        ParameterizedTypeReference<List<FabricVersionResponse>> responseType =
                new ParameterizedTypeReference<>() {
                };

        ResponseEntity<List<FabricVersionResponse>> response =
                restTemplate.exchange(getApiUrl() + "game", HttpMethod.GET, null, responseType);

        if (response.getBody() != null) {
            return response.getBody().stream().filter(FabricVersionResponse::isStable).map(FabricVersionResponse::getVersion).toList();
        } else {
            throw new RuntimeException("Error getting Fabric versions");
        }
    }

    private String getLatestInstallerVersion() {
        ParameterizedTypeReference<List<FabricInstallerResponse>> responseType =
                new ParameterizedTypeReference<>() {
                };

        ResponseEntity<List<FabricInstallerResponse>> response =
                restTemplate.exchange(getApiUrl() + "installer", HttpMethod.GET, null, responseType);

        if (response.getBody() != null) {
            return response.getBody().stream().filter(FabricInstallerResponse::isStable).map(FabricInstallerResponse::getVersion).toList().stream().findFirst().orElse(null);
        } else {
            throw new RuntimeException("Error getting Fabric versions");
        }
    }

    @Override
    protected List<String> getLoaderVersions() {
        ParameterizedTypeReference<List<FabricLoaderResponse>> responseType =
                new ParameterizedTypeReference<>() {
                };

        ResponseEntity<List<FabricLoaderResponse>> response =
                restTemplate.exchange(getApiUrl() + "loader", HttpMethod.GET, null, responseType);

        if (response.getBody() != null) {
            return response.getBody().stream().map(FabricLoaderResponse::getVersion).toList();
        } else {
            throw new RuntimeException("Error getting Fabric versions");
        }
    }

    @Override
    protected String getDownloadUrl(String version, String loaderVersion) {
        return getApiUrl() + "loader/" + version + "/" + loaderVersion + "/" + getLatestInstallerVersion() + "/server/jar";
    }
}
