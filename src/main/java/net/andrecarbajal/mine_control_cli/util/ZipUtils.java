package net.andrecarbajal.mine_control_cli.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ZipUtils {
    @Autowired
    ProgressBar progressBar;


    public void zipFolder(String sourceFolderPath, String zipFilePath) throws IOException {
        System.out.println("Starting backup ...");
        FileOutputStream fos = new FileOutputStream(zipFilePath);
        ZipOutputStream zos = new ZipOutputStream(fos);

        File sourceFolder = new File(sourceFolderPath);
        long totalSize = calculateTotalSize(sourceFolder);
        long[] compressedSize = {0};

        addFolderToZip(sourceFolder, sourceFolder.getName(), zos, totalSize, compressedSize);

        zos.close();
        fos.close();
        progressBar.reset();
    }

    private void addFolderToZip(File folder, String parentFolder, ZipOutputStream zos, long totalSize, long[] compressedSize) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFolderToZip(file, parentFolder + "/" + file.getName(), zos, totalSize, compressedSize);
                continue;
            }

            FileInputStream fis = new FileInputStream(file);
            String entryName = parentFolder + "/" + file.getName();
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
                compressedSize[0] += length;
                double progress = (double) compressedSize[0] / totalSize * 100;
                progressBar.display((int) progress);
            }

            fis.close();
        }
    }

    private long calculateTotalSize(File folder) {
        long totalSize = 0;
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                totalSize += calculateTotalSize(file);
            } else {
                totalSize += file.length();
            }
        }
        return totalSize;
    }
}
