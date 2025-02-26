package net.andrecarbajal.mine_control_cli.config.properties;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

@Getter
@Setter
public class ConfigProperties {
    private String serverRam = "2G";
    private String javaPath = "java";
    private Path instancesPath;
    private Path backupsPath;
}
