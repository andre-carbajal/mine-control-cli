package net.andrecarbajal.mine_control_cli.config.shell;

import lombok.AllArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.ApplicationProperties;
import org.jline.utils.AttributedString;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

@Configuration
@AllArgsConstructor
public class ShellConfiguration implements PromptProvider {
    private final ApplicationProperties applicationProperties;

    @Override
    public AttributedString getPrompt() {
        return new AttributedString(applicationProperties.getName() + ":>");
    }
}
