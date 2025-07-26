package net.andrecarbajal.mine_control_cli.service;

import lombok.RequiredArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.util.ConversionUtil;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import net.andrecarbajal.mine_control_cli.util.ProgressBar;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BackupService {
    private final ConfigurationManager configurationManager;
    private final ProgressBar progressBar;

    public void createBackup(String serverName, String backupName) {
        if (backupName == null || backupName.isBlank()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = LocalDateTime.now().format(formatter);
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
                        String zipEntryName = relPath.toString().replace(File.separatorChar, '/');

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

    public List<String> listBackups() {
        String backupsDir = configurationManager.getString("paths.backups");
        File backupsDirectory = new File(backupsDir);
        String[] backupFilesArray = backupsDirectory.list((current, name) -> new File(current, name).isFile() && name.endsWith(".zip"));
        if (backupFilesArray == null || backupFilesArray.length == 0) {
            return new ArrayList<>();
        }
        return List.of(backupFilesArray);
    }

    public List<String> listBackupsWithSize() {
        var backupFilesArray = listBackups();
        List<String> result = new ArrayList<>();
        for (String backupName : backupFilesArray) {
            File backupFile = new File(configurationManager.getString("paths.backups"), backupName);
            long sizeBytes = backupFile.length();
            String sizeStr = ConversionUtil.humanReadableByteCount(sizeBytes);
            result.add(backupName + " (" + sizeStr + ")");
        }
        return result;
    }

    public void deleteBackup(String backupName) {
        String backupsDir = configurationManager.getString("paths.backups");
        Path backupPath = Paths.get(backupsDir, backupName + ".zip");
        if (!Files.exists(backupPath)) {
            System.out.println(TextDecorationUtil.error("Backup does not exist: " + backupPath));
            return;
        }
        try {
            Files.delete(backupPath);
            System.out.println(TextDecorationUtil.success("Backup '" + backupName + "' deleted successfully."));
        } catch (Exception e) {
            System.out.println(TextDecorationUtil.error("Failed to delete backup '" + backupName + "': " + e.getMessage()));
        }
    }

    public void restoreBackup(String serverName, String backupName) {
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

        try {
            FileUtil.cleanDirectory(serverPath);
            try (ZipFile zipFile = ZipFile.builder().setFile(backupPath.toFile()).setUseUnicodeExtraFields(true).get()) {
                var entriesEnum = zipFile.getEntries();
                List<ZipArchiveEntry> entryList = new ArrayList<>();
                while (entriesEnum.hasMoreElements()) {
                    entryList.add(entriesEnum.nextElement());
                }
                int totalEntries = entryList.size();
                progressBar.reset();
                int[] processed = {0};

                for (ZipArchiveEntry entry : entryList) {
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
    }
}