package net.andrecarbajal.mine_control_cli.service;

import lombok.extern.slf4j.Slf4j;
import net.andrecarbajal.mine_control_cli.util.FileUtil;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

@Slf4j
@Service
public class FileDownloadService {
    public void download(String url, String serverName) {
        Path serverPath = FileUtil.getMineControlCliFolder().resolve(serverName);

        try {
            URLConnection connection = new URL(url).openConnection();
            int fileSize = connection.getContentLength();

            try (ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
                 FileOutputStream fos = new FileOutputStream(serverPath.toFile())) {

                ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
                int bytesRead;
                int totalBytesRead = 0;

                while ((bytesRead = rbc.read(buffer)) != -1) {
                    buffer.flip();
                    fos.getChannel().write(buffer);
                    buffer.clear();

                    totalBytesRead += bytesRead;
                    double progress = (double) totalBytesRead / fileSize * 100;
                    log.info(String.format("Downloading... %.2f%%", progress), progress);
                }
            }
        } catch (IOException e) {
            String errorMessage = String.format("Error downloading file for server '%s': %s",
                    serverName, e.getMessage());
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }

    }
}
