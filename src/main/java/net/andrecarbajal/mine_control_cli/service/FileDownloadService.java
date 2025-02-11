package net.andrecarbajal.mine_control_cli.service;

import lombok.extern.slf4j.Slf4j;
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
    public void download(String url, Path serverPath) {
        Path path = serverPath.resolve("server.jar");

        try {
            URLConnection connection = new URL(url).openConnection();
            int fileSize = connection.getContentLength();

            try (ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream()); FileOutputStream fos = new FileOutputStream(path.toFile())) {

                ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
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
            throw new RuntimeException(String.format("Error downloading file for server %s", e.getMessage()), e);
        }

    }
}
