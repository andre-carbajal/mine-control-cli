package net.andrecarbajal.mine_control_cli.service.server;

import net.andrecarbajal.mine_control_cli.exception.ServerCreationException;
import net.andrecarbajal.mine_control_cli.model.paper.PaperBuildResponse;
import net.andrecarbajal.mine_control_cli.model.paper.PaperResponse;
import net.andrecarbajal.mine_control_cli.service.download.FileDownloadService;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PaperService implements ILoaderService {
    @Autowired
    private FileDownloadService fileDownloadService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "https://api.papermc.io/v2/projects/paper";

    @Override
    public void createServer(String serverName, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        List<SelectorItem<String>> items = getVersions().stream().map(version -> SelectorItem.of(version, version)).toList();

        SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(terminal, items, "Minecraft Version", null);
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);
        SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector.run(SingleItemSelector.SingleItemSelectorContext.empty());
        String version = context.getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).get();

        System.out.printf("Creating a Paper server, name: %s, version: %s \n", serverName, version);

        Path serverPath = FileUtil.getServerInstancesFolder().resolve(serverName);

        try {
            FileUtil.createFolder(serverPath);
            fileDownloadService.download(getDownloadUrl(version, getLatestBuild(version)), serverPath);
            FileUtil.saveEulaFile(serverPath);
        } catch (Exception e) {
            FileUtil.deleteFolder(serverPath);
            throw new ServerCreationException("Error creating server", e);
        }
        System.out.println("Paper server created successfully");
    }

    private List<String> getVersions() {
        PaperResponse response = restTemplate.getForObject(apiUrl, PaperResponse.class);

        if (response != null && response.getVersions() != null) {
            var versions = response.getVersions();
            Collections.reverse(versions);
            return versions;
        }
        throw new RuntimeException("Error getting Paper versions");
    }

    public String getLatestBuild(String version) {
        PaperBuildResponse response = restTemplate.getForObject(apiUrl + "/versions/" + version, PaperBuildResponse.class);

        if (response != null && response.getBuilds() != null) {
            return String.valueOf(response.getBuilds().length - 1);
        }
        throw new RuntimeException("Error getting Paper builds");
    }

    private String getDownloadUrl(String version, String latestBuild) {
        return apiUrl + "/versions/" + version + "/builds/" + latestBuild + "/downloads/paper-" + version + "-" + latestBuild + ".jar";
    }
}
