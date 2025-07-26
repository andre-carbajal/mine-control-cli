package net.andrecarbajal.mine_control_cli.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class ApplicationProperties {
    private Update update = new Update();
    private Paths paths = new Paths();
    private Java java = new Java();
    private Eula eula = new Eula();
    private PotatoPeeler potatoPeeler = new PotatoPeeler();

    @Data
    public static class Update {
        private boolean checkOnStartup;
    }

    @Data
    public static class Paths {
        private String baseDir;
        private String serversDir;
        private String backupsDir;
    }

    @Data
    public static class Java {
        private String path;
        private String minRam;
        private String maxRam;
    }

    @Data
    public static class Eula {
        private boolean autoAccept;
    }

    @Data
    public static class PotatoPeeler {
        private boolean enabledServerStartup;
        private int chunkInhabitedTime;
    }
}
