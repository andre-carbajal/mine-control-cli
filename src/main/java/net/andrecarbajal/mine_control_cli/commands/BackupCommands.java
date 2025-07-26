package net.andrecarbajal.mine_control_cli.commands;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.service.BackupService;
import net.andrecarbajal.mine_control_cli.service.ServerManagerService;
import net.andrecarbajal.mine_control_cli.util.ComponentUtil;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class BackupCommands extends AbstractShellComponent {
    private final ServerManagerService serverManagerService;
    private final BackupService backupService;

    @ShellMethod(key = "backup create", value = "Create a backup of the server")
    public void createBackup(
            @ShellOption(help = "The name of the backup", defaultValue = ShellOption.NULL) String backupName,
            @ShellOption(help = "The server to backup", defaultValue = ShellOption.NULL) String serverName
    ) {
        if (serverName == null) {
            List<String> serverNames = serverManagerService.listServers();
            serverName = ComponentUtil.selectServer(
                    serverNames,
                    "Select the server to backup:",
                    getTerminal(),
                    getResourceLoader(),
                    getTemplateExecutor());
            if (serverName == null) {
                System.out.println(TextDecorationUtil.error("No server selected for backup."));
                return;
            }
        }

        if (backupName == null) {
            backupName = ComponentUtil.inputString(
                    "Enter the name of the backup (or leave empty for default):",
                    getTerminal(),
                    getResourceLoader(),
                    getTemplateExecutor());
        }

        backupService.createBackup(serverName, backupName);
    }

    @ShellMethod(key = "backup list", value = "List all backups")
    public void listBackups() {
        List<String> backupFiles = backupService.listBackupsWithSize();
        if (backupFiles.isEmpty()) {
            System.out.println(TextDecorationUtil.info("There are no backups available."));
            return;
        }
        System.out.println(TextDecorationUtil.green("=== Available Backups ==="));
        for (int i = 0; i < backupFiles.size(); i++) {
            System.out.println(TextDecorationUtil.cyan(String.format("  %2d.", i + 1)) + " " + backupFiles.get(i));
        }
        System.out.println(TextDecorationUtil.green("========================="));
    }

    @ShellMethod(key = "backup delete", value = "Delete a backup")
    public void deleteBackup(
            @ShellOption(help = "Name of the backup to delete (Without .zip extension)", defaultValue = ShellOption.NULL) String backupName
    ) {
        if (backupName == null) {
            List<String> backups = backupService.listBackups();
            backupName = ComponentUtil.selectBackup(
                    backups,
                    "Select the backup to delete:",
                    getTerminal(),
                    getResourceLoader(),
                    getTemplateExecutor());
            if (backupName == null) {
                System.out.println(TextDecorationUtil.error("No backup selected for deletion."));
                return;
            }
        }

        if (ComponentUtil.confirm("Are you sure you want to delete the backup '" + backupName + "'? This action cannot be undone.", getTerminal(), getResourceLoader(), getTemplateExecutor())) {
            backupService.deleteBackup(backupName);
        } else {
            System.out.println(TextDecorationUtil.info("Backup deletion cancelled."));
        }
    }

    @ShellMethod(key = "backup restore", value = "Restore a backup")
    public void restoreBackup(
            @ShellOption(help = "Name of the backup to restore (Without .zip extension)", defaultValue = ShellOption.NULL) String backupName,
            @ShellOption(help = "Server to restore the backup to", defaultValue = ShellOption.NULL) String serverName
    ) {
        if (backupName == null) {
            List<String> backupFiles = backupService.listBackups();
            backupName = ComponentUtil.selectBackup(
                    backupFiles,
                    "Select the backup to restore:",
                    getTerminal(),
                    getResourceLoader(),
                    getTemplateExecutor());
            if (backupName == null) {
                System.out.println(TextDecorationUtil.error("No backup selected for restoration."));
                return;
            }
        }
        if (serverName == null) {
            List<String> serverNames = serverManagerService.listServers();
            serverName = ComponentUtil.selectServer(
                    serverNames,
                    "Select the server to restore the backup to:",
                    getTerminal(),
                    getResourceLoader(),
                    getTemplateExecutor());
            if (serverName == null) {
                System.out.println(TextDecorationUtil.error("No server selected for restoration."));
                return;
            }
        }

        if (ComponentUtil.confirm("Are you sure you want to restore the backup '" + backupName + "' to server '" + serverName + "'? This action will DELETE all current files and overwrite with the backup.", getTerminal(), getResourceLoader(), getTemplateExecutor())) {
            backupService.restoreBackup(serverName, backupName);
        } else {
            System.out.println(TextDecorationUtil.info("Backup restoration cancelled."));
        }
    }
}
