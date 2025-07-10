package net.andrecarbajal.mine_control_cli.service.server.base;

import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.style.TemplateExecutor;

public interface ServerCreator {
    void createServer(String serverName, String loaderVersion, String minecraftVersion, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor);
}

