package net.andrecarbajal.mine_control_cli.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.application")
@Data
public class SpringApplicationProperties {
    private String name;
    private String version;
}
