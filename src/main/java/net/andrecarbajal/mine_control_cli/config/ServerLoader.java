package net.andrecarbajal.mine_control_cli.config;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ServerLoader {
    VANILLA,
    PAPER;

    public static List<String> getLoaders() {
        return Arrays.stream(ServerLoader.values())
                .map(enumValue -> StringUtils.capitalize(enumValue.name().toLowerCase()))
                .collect(Collectors.toList());
    }
}
