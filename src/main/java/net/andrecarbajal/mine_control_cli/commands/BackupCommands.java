package net.andrecarbajal.mine_control_cli.commands;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.util.ComponentUtil;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.ProgressBar;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@ShellComponent
@RequiredArgsConstructor
public class BackupCommands extends AbstractShellComponent {
    private final ConfigurationManager configurationManager;
    private final ProgressBar progressBar;

    @ShellMethod(key = "backup create", value = "Create a backup of the server")
    public void createBackup(
            @ShellOption(help = "The name of the backup", defaultValue = ShellOption.NULL) String backupName,
            @ShellOption(help = "The server to backup", defaultValue = ShellOption.NULL) String serverName
    ) {
        if (backupName == null) {
            backupName = ComponentUtil.inputString(
                    "Enter the name of the backup (or leave empty for default):",
                    getTerminal(),
                    getResourceLoader(),
                    getTemplateExecutor());
        }
        if (serverName == null) {
            serverName = ComponentUtil.selectServer(
                    "Select the server to backup:",
                    configurationManager,
                    getTerminal(),
                    getResourceLoader(),
                    getTemplateExecutor());
            if (serverName == null) {
                System.out.println(TextDecorationUtil.error("No server selected for backup."));
                return;
            }
        }

        if (backupName == null || backupName.isBlank()) {
            DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = java.time.LocalDateTime.now().format(formatter);
            backupName = serverName + "_" + timestamp;
        }

        String serversDir = configurationManager.getString("paths.servers");
        String backupsDir = configurationManager.getString("paths.backups");
        Path serverPath = Paths.get(serversDir, serverName);
        Path backupPath = Paths.get(backupsDir, backupName + ".zip");
        if (!Files.exists(serverPath)) {
            System.out.println(TextDecorationUtil.error("The specified server does not exist: " + serverPath));
            return;
        }
        try {
            long totalFiles;
            try (Stream<Path> countStream = Files.walk(serverPath)) {
                totalFiles = countStream.filter(Files::isRegularFile).count();
            }
            if (totalFiles == 0) {
                System.out.println(TextDecorationUtil.error("There are no files to backup in: " + serverPath));
                return;
            }
            AtomicInteger processedFiles = new AtomicInteger(0);
            progressBar.reset();
            try (OutputStream fos = Files.newOutputStream(backupPath);
                 ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(fos);
                 Stream<Path> walk = Files.walk(serverPath)) {
                walk.forEach(path -> {
                    try {
                        Path relPath = serverPath.relativize(path);
                        String zipEntryName = relPath.toString().replace(java.io.File.separatorChar, '/');
                        if (Files.isDirectory(path)) {
                            if (!zipEntryName.isEmpty()) {
                                ZipArchiveEntry entry = new ZipArchiveEntry(zipEntryName + "/");
                                zipOut.putArchiveEntry(entry);
                                zipOut.closeArchiveEntry();
                            }
                        } else {
                            ZipArchiveEntry entry = new ZipArchiveEntry(zipEntryName);
                            zipOut.putArchiveEntry(entry);
                            Files.copy(path, zipOut);
                            zipOut.closeArchiveEntry();
                            int percent = (int) ((processedFiles.incrementAndGet() * 100) / totalFiles);
                            progressBar.display(percent, "Compressing: " + path.getFileName());
                        }
                    } catch (Exception e) {
                        System.out.println(TextDecorationUtil.error("Error adding file to backup: " + path + " - " + e.getMessage()));
                    }
                });
                zipOut.finish();
                progressBar.display(100, "Backup completed");
                System.out.println(TextDecorationUtil.success("Backup created successfully: " + backupPath));
            }
        } catch (Exception e) {
            System.out.println(TextDecorationUtil.error("Error creating backup: " + e.getMessage()));
        }
    }

    @ShellMethod(key = "backup list", value = "List all backups")
    public void listBackups() {
        String backupsDir = configurationManager.getString("paths.backups");
        try {
            File backupsDirectory = new File(backupsDir);
            String[] backupFilesArray = backupsDirectory.list((current, name) -> new File(current, name).isFile() && name.endsWith(".zip"));
            if (backupFilesArray == null || backupFilesArray.length == 0) {
                System.out.println(TextDecorationUtil.info("There are no backups available."));
                return;
            }
            List<String> backupFiles = java.util.Arrays.asList(backupFilesArray);
            System.out.println(TextDecorationUtil.green("=== Available Backups ==="));
            for (int i = 0; i < backupFiles.size(); i++) {
                System.out.println(TextDecorationUtil.cyan(String.format("  %2d.", i + 1)) + " " + backupFiles.get(i));
            }
            System.out.println(TextDecorationUtil.green("========================="));
        } catch (Exception e) {
            System.out.println(TextDecorationUtil.error("Error listing backups: " + e.getMessage()));
        }
    }

    @ShellMethod(key = "backup delete", value = "Delete a backup")
    public void deleteBackup(
            @ShellOption(help = "Name of the backup to delete (Without .zip extension)", defaultValue = ShellOption.NULL) String backupName
    ) {
        if (backupName == null) {
            backupName = ComponentUtil.selectBackup(
                    "Select the backup to delete:",
                    configurationManager,
                    getTerminal(),
                    getResourceLoader(),
                    getTemplateExecutor());
            if (backupName == null) {
                System.out.println(TextDecorationUtil.error("No backup selected for deletion."));
                return;
            }
        }

        String backupsDir = configurationManager.getString("paths.backups");
        Path backupPath = Paths.get(backupsDir, backupName + ".zip");
        if (!Files.exists(backupPath)) {
            System.out.println(TextDecorationUtil.error("Backup does not exist: " + backupPath));
            return;
        }

        if (ComponentUtil.confirm("Are you sure you want to delete the backup '" + backupName + "'? This action cannot be undone.", getTerminal(), getResourceLoader(), getTemplateExecutor())) {
            try {
                Files.delete(backupPath);
                System.out.println(TextDecorationUtil.success("Backup '" + backupName + "' deleted successfully."));
            } catch (Exception e) {
                System.out.println(TextDecorationUtil.error("Failed to delete backup '" + backupName + "': " + e.getMessage()));
            }
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
            backupName = ComponentUtil.selectBackup(
                    "Select the backup to restore:",
                    configurationManager,
                    getTerminal(),
                    getResourceLoader(),
                    getTemplateExecutor());
            if (backupName == null) {
                System.out.println(TextDecorationUtil.error("No backup selected for restoration."));
                return;
            }
        }
        if (serverName == null) {
            serverName = ComponentUtil.selectServer(
                    "Select the server to restore the backup to:",
                    configurationManager,
                    getTerminal(),
                    getResourceLoader(),
                    getTemplateExecutor());
            if (serverName == null) {
                System.out.println(TextDecorationUtil.error("No server selected for restoration."));
                return;
            }
        }

        String backupsDir = configurationManager.getString("paths.backups");
        String serversDir = configurationManager.getString("paths.servers");
        Path backupPath = Paths.get(backupsDir, backupName + ".zip");
        Path serverPath = Paths.get(serversDir, serverName);

        if (!Files.exists(backupPath)) {
            System.out.println(TextDecorationUtil.error("Backup does not exist: " + backupPath));
            return;
        }

        if (!Files.exists(serverPath)) {
            System.out.println(TextDecorationUtil.error("Server does not exist: " + serverPath));
            return;
        }

        if (ComponentUtil.confirm("Are you sure you want to restore the backup '" + backupName + "' to server '" + serverName + "'? This action will DELETE all current files and overwrite with the backup.", getTerminal(), getResourceLoader(), getTemplateExecutor())) {
            try {
                FileUtil.cleanDirectory(serverPath);
                try (ZipFile zipFile = ZipFile.builder().setFile(backupPath.toFile()).setUseUnicodeExtraFields(true).get()) {
                    var entriesEnum = zipFile.getEntries();
                    int totalEntries;
                    List<ZipArchiveEntry> entryList = new ArrayList<>();
                    while (entriesEnum.hasMoreElements()) {
                        entryList.add(entriesEnum.nextElement());
                    }
                    totalEntries = entryList.size();
                    progressBar.reset();
                    int[] processed = {0};
                    for (org.apache.commons.compress.archivers.zip.ZipArchiveEntry entry : entryList) {
                        try {
                            Path outPath = serverPath.resolve(entry.getName());
                            if (entry.isDirectory()) {
                                Files.createDirectories(outPath);
                            } else {
                                Files.createDirectories(outPath.getParent());
                                try (var in = zipFile.getInputStream(entry)) {
                                    Files.copy(in, outPath, StandardCopyOption.REPLACE_EXISTING);
                                }
                            }
                            processed[0]++;
                            int percent = (int) ((processed[0] * 100.0) / totalEntries);
                            progressBar.display(percent, "Restoring: " + entry.getName());
                        } catch (Exception e) {
                            System.out.println(TextDecorationUtil.error("Error extracting: " + entry.getName() + " - " + e.getMessage()));
                        }
                    }
                    progressBar.display(100, "Restore completed");
                }
                System.out.println(TextDecorationUtil.success("Backup '" + backupName + "' restored successfully to server '" + serverName + "'."));
            } catch (Exception e) {
                System.out.println(TextDecorationUtil.error("Failed to restore backup: " + e.getMessage()));
            }
        } else {
            System.out.println(TextDecorationUtil.info("Backup restoration cancelled."));
        }
    }
}
