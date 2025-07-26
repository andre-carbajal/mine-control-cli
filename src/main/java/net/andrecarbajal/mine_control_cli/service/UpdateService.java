package net.andrecarbajal.mine_control_cli.service;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.properties.SpringApplicationProperties;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdateService {
    private static final String GITHUB_USERNAME = "MineControlCli";
    private static final String GITHUB_REPOSITORY = "mine-control-cli";
    private static final String DOWNLOAD_URL = String.format("https://github.com/%s/%s/releases", GITHUB_USERNAME, GITHUB_REPOSITORY);

    private final GithubService githubService = new GithubService(GITHUB_USERNAME, GITHUB_REPOSITORY);
    private final SpringApplicationProperties springApplicationProperties;

    public void showUpdateMessage(boolean showLatestVersion) {
        Optional<String> updateInfoOpt = githubService.getLatestReleaseTag();
        if (updateInfoOpt.isPresent() && githubService.isCurrentVersionLatest(springApplicationProperties.getVersion())) {
            System.out.println(TextDecorationUtil.info("A new version is available: ") + TextDecorationUtil.cyan(updateInfoOpt.get()));
            System.out.println(TextDecorationUtil.info("Download it at: ") + TextDecorationUtil.green(DOWNLOAD_URL));
        } else if (showLatestVersion) {
            System.out.println(TextDecorationUtil.success("You are using the latest version: ") + TextDecorationUtil.cyan(springApplicationProperties.getVersion()));
        }
    }
}
