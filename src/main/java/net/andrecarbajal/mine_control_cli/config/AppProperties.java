package net.andrecarbajal.mine_control_cli.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Getter
@Setter
@Order(1)
@Configuration
@ConfigurationProperties(prefix = "spring.application")
public class AppProperties {
    private String name;
    private String version;
}
