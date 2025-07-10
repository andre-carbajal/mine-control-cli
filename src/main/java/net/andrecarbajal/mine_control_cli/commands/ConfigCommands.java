package net.andrecarbajal.mine_control_cli.commands;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.util.ComponentUtil;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.ArrayList;
import java.util.Map;

@ShellComponent
@RequiredArgsConstructor
public class ConfigCommands extends AbstractShellComponent {
    private final ConfigurationManager configurationManager;

    @ShellMethod(key = "config set", value = "Set a configuration property")
    public void set(
            @ShellOption(help = "The key of the property to set", defaultValue = ShellOption.NULL) String key,
            @ShellOption(help = "The value to set for the property", defaultValue = ShellOption.NULL) String value) {

        if (key == null) {
            key = ComponentUtil.selectString(
                    new ArrayList<>(configurationManager.getAllProperties().keySet()),
                    "Select a configuration key to set:",
                    getTerminal(),
                    getResourceLoader(),
                    getTemplateExecutor());
            if (key == null) {
                System.out.println(TextDecorationUtil.info("No configuration key selected."));
                return;
            }
        }

        if (!configurationManager.hasProperty(key)) {
            System.out.println(TextDecorationUtil.error("The key '" + key + "' does not exist. Use 'config list' to see available keys."));
            return;
        }

        String currentValue = configurationManager.getString(key);
        System.out.println(TextDecorationUtil.info("Actual value for '" + key + "': " + currentValue));

        if (value == null) {
            value = ComponentUtil.inputString("Enter new value for '" + key + "':", getTerminal(), getResourceLoader(), getTemplateExecutor());
            if (value == null || value.isBlank()) {
                System.out.println(TextDecorationUtil.info("No value provided for key '" + key + "'."));
                return;
            }
        }

        if (value.equals(currentValue)) {
            System.out.println(TextDecorationUtil.info("The value for '" + key + "' is already set to '" + value + "'. No changes were made."));
            return;
        }

        boolean success = configurationManager.setProperty(key, value);
        if (!success) {
            System.out.println(TextDecorationUtil.error("Failed to update the configuration property '" + key + "'."));
            return;
        }
        System.out.println(TextDecorationUtil.success("Configuration property '" + key + "' updated from '" + currentValue + "' to '" + value + "'."));
    }

    @ShellMethod(key = "config get", value = "Get the value of a configuration property")
    public void get(@ShellOption(
            help = "The key of the property to get", defaultValue = ShellOption.NULL) String key) {
        if (key == null) {
            key = ComponentUtil.selectString(
                    new ArrayList<>(configurationManager.getAllProperties().keySet()),
                    "Select a configuration key:",
                    getTerminal(),
                    getResourceLoader(),
                    getTemplateExecutor());
            if (key == null) {
                System.out.println(TextDecorationUtil.info("No configuration key selected."));
                return;
            }
        }

        if (configurationManager.hasProperty(key)) {
            System.out.println(TextDecorationUtil.cyan(key + " = " + configurationManager.getString(key)));
        } else {
            System.out.println(TextDecorationUtil.error("The key '" + key + "' does not exist."));
        }
    }

    @ShellMethod(key = {"config list", "config ls"}, value = "List all configuration properties")
    public void list() {
        System.out.println(TextDecorationUtil.info("Configuration properties:"));
        for (Map.Entry<String, String> entry : configurationManager.getAllProperties().entrySet()) {
            String coloredKey = TextDecorationUtil.cyan(entry.getKey());
            String coloredValue = TextDecorationUtil.green(entry.getValue());
            System.out.println(coloredKey + " = " + coloredValue);
        }
    }

    @ShellMethod(key = "config reset", value = "Reset configuration to default values")
    public void reset() {
        configurationManager.resetToDefaults();
        System.out.println(TextDecorationUtil.success("Configuration reset to default values."));
    }
}