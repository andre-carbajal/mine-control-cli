package net.andrecarbajal.mine_control_cli.util;

import lombok.experimental.UtilityClass;
import net.andrecarbajal.mine_control_cli.model.LoaderType;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.component.ConfirmationInput;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.StringInput;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.style.TemplateExecutor;

import java.util.List;
import java.util.function.Function;

@UtilityClass
public class ComponentUtil {

    public String selectString(List<String> keys, String prompt, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        List<SelectorItem<String>> items = keys.stream().map(key -> SelectorItem.of(key, key)).toList();
        return selectGeneric(items, prompt, SelectorItem::getName, "", terminal, resourceLoader, templateExecutor);
    }

    public LoaderType selectLoaderType(Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        List<SelectorItem<LoaderType>> items = java.util.Arrays.stream(LoaderType.values()).map(loader -> SelectorItem.of(loader.getDisplayName(), loader)).toList();
        return selectGeneric(items, "Select a server loader type:", SelectorItem::getItem, LoaderType.VANILLA, terminal, resourceLoader, templateExecutor);
    }

    public String selectServer(List<String> serverNames, String prompt, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        if (serverNames.isEmpty()) {
            System.out.println(TextDecorationUtil.error("There are no servers available."));
            return null;
        }
        List<SelectorItem<String>> items = serverNames.stream().map(name -> SelectorItem.of(name, name)).toList();
        return selectGeneric(items, prompt, SelectorItem::getName, null, terminal, resourceLoader, templateExecutor);
    }

    public String selectBackup(List<String> backupFiles, String prompt, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        if (backupFiles.isEmpty()) {
            System.out.println(TextDecorationUtil.error("There are no backups available."));
            return null;
        }
        List<String> backupNames = backupFiles.stream()
                .map(name -> name.endsWith(".zip") ? name.substring(0, name.length() - 4) : name)
                .toList();
        List<SelectorItem<String>> items = backupNames.stream().map(name -> SelectorItem.of(name, name)).toList();
        return selectGeneric(items, prompt, SelectorItem::getName, null, terminal, resourceLoader, templateExecutor);
    }

    public String inputString(String prompt, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        StringInput input = createStringInput(prompt, terminal, resourceLoader, templateExecutor);
        StringInput.StringInputContext context = input.run(StringInput.StringInputContext.empty());
        return context.getResultValue();
    }

    public boolean confirm(String prompt, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        ConfirmationInput confirmation = createConfirmationInput(prompt, terminal, resourceLoader, templateExecutor);
        return confirmation.run(ConfirmationInput.ConfirmationInputContext.empty()).getResultValue();
    }

    private <T> T selectGeneric(List<SelectorItem<T>> items, String prompt, Function<SelectorItem<T>, T> resultExtractor, T defaultValue, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        if (items == null || items.isEmpty()) {
            System.out.println("No items available to select.");
            return defaultValue;
        }
        SingleItemSelector<T, SelectorItem<T>> selector = createSelector(items, prompt, terminal, resourceLoader, templateExecutor);
        SingleItemSelector.SingleItemSelectorContext<T, SelectorItem<T>> context = selector.run(SingleItemSelector.SingleItemSelectorContext.empty());
        return context.getResultItem().map(resultExtractor).orElse(defaultValue);
    }

    private <T, S extends SelectorItem<T>> SingleItemSelector<T, S> createSelector(List<S> items, String prompt, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        SingleItemSelector<T, S> selector = new SingleItemSelector<>(terminal, items, prompt, null);
        selector.setMaxItems(15);
        selector.setResourceLoader(resourceLoader);
        selector.setTemplateExecutor(templateExecutor);
        return selector;
    }

    private StringInput createStringInput(String prompt, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        StringInput input = new StringInput(terminal, prompt, "");
        input.setResourceLoader(resourceLoader);
        input.setTemplateExecutor(templateExecutor);
        return input;
    }

    private ConfirmationInput createConfirmationInput(String prompt, Terminal terminal, ResourceLoader resourceLoader, TemplateExecutor templateExecutor) {
        ConfirmationInput confirmation = new ConfirmationInput(terminal, prompt, false);
        confirmation.setResourceLoader(resourceLoader);
        confirmation.setTemplateExecutor(templateExecutor);
        return confirmation;
    }
}
