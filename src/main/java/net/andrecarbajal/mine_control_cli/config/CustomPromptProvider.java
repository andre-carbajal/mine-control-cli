package net.andrecarbajal.mine_control_cli.config;

import lombok.AllArgsConstructor;
import org.jline.utils.AttributedString;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

@Configuration
@AllArgsConstructor
public class CustomPromptProvider implements PromptProvider {
    private final AppProperties appProperties;

    @Override
    public AttributedString getPrompt() {
        return new AttributedString(appProperties.getName() + ":>");
    }
}
