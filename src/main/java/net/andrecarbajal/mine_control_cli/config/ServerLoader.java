package net.andrecarbajal.mine_control_cli.config;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public enum ServerLoader {
    VANILLA,
    PAPER;

    public static ServerLoader getLoader(String loader) {
        return Arrays.stream(ServerLoader.values())
                .filter(enumValue -> enumValue.name().equalsIgnoreCase(loader))
                .findFirst()
                .orElse(null);
    }

    public static List<String> getStringLoader() {
        return Arrays.stream(ServerLoader.values())
                .map(enumValue -> StringUtils.capitalize(enumValue.name().toLowerCase()))
                .toList();
    }
}
