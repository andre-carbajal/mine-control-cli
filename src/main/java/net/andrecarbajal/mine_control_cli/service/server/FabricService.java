package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.model.fabric.FabricInstallerResponse;
import net.andrecarbajal.mine_control_cli.model.fabric.FabricLoaderResponse;
import net.andrecarbajal.mine_control_cli.model.fabric.FabricVersionResponse;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FabricService extends AbstractMinecraftService {
    public FabricService(FileDownloadService fileDownloadService) {
        super(fileDownloadService);
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

        return response.getBody().stream().filter(FabricVersionResponse::isStable).map(FabricVersionResponse::getVersion).toList();
    }

    private String getLatestInstallerVersion() {
        ParameterizedTypeReference<List<FabricInstallerResponse>> responseType =
                new ParameterizedTypeReference<>() {
                };

        ResponseEntity<List<FabricInstallerResponse>> response =
                restTemplate.exchange(getApiUrl() + "installer", HttpMethod.GET, null, responseType);

        return response.getBody().stream().filter(FabricInstallerResponse::isStable).map(FabricInstallerResponse::getVersion).toList().stream().findFirst().orElse(null);
    }

    public String getLatestLoaderVersion() {
        ParameterizedTypeReference<List<FabricLoaderResponse>> responseType =
                new ParameterizedTypeReference<>() {
                };

        ResponseEntity<List<FabricLoaderResponse>> response =
                restTemplate.exchange(getApiUrl() + "loader", HttpMethod.GET, null, responseType);

        return response.getBody().stream().filter(FabricLoaderResponse::isStable).map(FabricLoaderResponse::getVersion).toList().stream().findFirst().orElse(null);
    }

    @Override
    protected String getDownloadUrl(String version) {
        return getApiUrl() + "loader/" + version + "/" + getLatestLoaderVersion() + "/" + getLatestInstallerVersion() + "/server/jar";
    }
}
