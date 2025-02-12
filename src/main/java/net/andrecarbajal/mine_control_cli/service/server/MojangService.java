package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.exception.ServerCreationException;
import net.andrecarbajal.mine_control_cli.model.mojang.MojangServerResponse;
import net.andrecarbajal.mine_control_cli.model.mojang.MojangVersionsResponse;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.style.TemplateExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public abstract class MojangService implements ILoaderService {
    @Autowired
    private FileDownloadService fileDownloadService;

    public abstract String type();

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    @Override
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

        System.out.printf("Creating a %s server, name: %s, version: %s \n", StringUtils.capitalize(type()), serverName, version);

        Path serverPath = FileUtil.getMineControlCliFolder().resolve(serverName);

        try {
            FileUtil.createFolder(serverPath);
            fileDownloadService.download(getDownloadUrl(version), serverPath);
            FileUtil.saveEulaFile(serverPath);
        } catch (Exception e) {
            FileUtil.deleteFolder(serverPath);
            throw new ServerCreationException("Error creating server", e);
        }
        System.out.printf("%s server created successfully \n", StringUtils.capitalize(type()));
    }

    private List<String> getVersions() {
        MojangVersionsResponse response = restTemplate.getForObject(apiUrl, MojangVersionsResponse.class);

        if (response != null && response.getVersions() != null) {
            return response.getVersions().stream()
                    .filter(version -> version.getType().equals(type()))
                    .map(MojangVersionsResponse.Version::getId).toList();
        } else {
            throw new RuntimeException("Failed to retrieve versions from Mojang API");
        }
    }

    private String getDownloadUrl(String version) {
        MojangVersionsResponse versionsResponse = restTemplate.getForObject(apiUrl, MojangVersionsResponse.class);
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
