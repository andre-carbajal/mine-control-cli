package net.andrecarbajal.mine_control_cli.service.minecraft;

import lombok.extern.slf4j.Slf4j;
import net.andrecarbajal.mine_control_cli.exception.ServerCreationException;
import net.andrecarbajal.mine_control_cli.model.vanilla.VanillaServerResponse;
import net.andrecarbajal.mine_control_cli.model.vanilla.VanillaVersionsResponse;
import net.andrecarbajal.mine_control_cli.service.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.style.TemplateExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class VanillaService {
    @Autowired
    private FileDownloadService fileDownloadService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    public void createServer(String serverName, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        List<SelectorItem<String>> items = getVersions().stream()
                .map(version -> SelectorItem.of(version, version))
                .toList();

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(terminal, items, "Minecraft Version", null);
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);
        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector
                .run(SingleItemSelector.SingleItemSelectorContext.empty());
        String version = context.getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).get();

        Path serverPath = FileUtil.getMineControlCliFolder().resolve(serverName);

        try {
            FileUtil.createFolder(serverPath);
            FileUtil.saveEulaFile(serverPath);
            fileDownloadService.download(getDownloadUrl(version), serverPath);
        } catch (Exception e) {
            throw new ServerCreationException("Error creating server", e);
        }

        System.out.println("Creating a Vanilla server, name: " + serverName + ", version: " + version);
    }

    private List<String> getVersions() {
        VanillaVersionsResponse response = restTemplate.getForObject(apiUrl, VanillaVersionsResponse.class);

        if (response != null && response.getVersions() != null) {
            return response.getVersions().stream()
                    .filter(version -> version.getType().equals("release"))
                    .map(VanillaVersionsResponse.VanillaVersion::getId).toList();
        } else {
            log.error("Failed to retrieve versions from Mojang API");
            throw new RuntimeException("Failed to retrieve versions from Mojang API");
        }
    }

    private String getDownloadUrl(String version) {
        VanillaVersionsResponse versionsResponse = restTemplate.getForObject(apiUrl, VanillaVersionsResponse.class);
        if (versionsResponse != null && versionsResponse.getVersions() != null) {
            String url = versionsResponse.getVersions().stream()
                    .filter(vanillaVersion -> vanillaVersion.getId().equals(version))
                    .map(VanillaVersionsResponse.VanillaVersion::getUrl)
                    .findFirst().orElse(null);
            if (url != null) {
                VanillaServerResponse serverResponse = restTemplate.getForObject(url, VanillaServerResponse.class);
                if (serverResponse != null && serverResponse.getDownloads() != null && serverResponse.getDownloads().getServer() != null) {
                    return serverResponse.getDownloads().getServer().getUrl();
                } else {
                    throw new RuntimeException("Unable to retrieve server download URL from Mojang API");
                }
            }
            throw new RuntimeException("Unable to retrieve server download URL from Mojang API");

        } else {
            log.error("Failed to retrieve server download URL from Mojang API");
            throw new RuntimeException("Failed to retrieve server download URL from Mojang API");
        }

    }
}
