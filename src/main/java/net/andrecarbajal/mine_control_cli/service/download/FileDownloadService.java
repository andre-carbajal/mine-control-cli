package net.andrecarbajal.mine_control_cli.service.download;

import lombok.AllArgsConstructor;
import net.andrecarbajal.mine_control_cli.util.ui.ProgressBar;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

@Service
@AllArgsConstructor
public class FileDownloadService {
    private ProgressBar progressBar;

    public void downloadFile(String url, Path serverPath, String fileName) {
        System.out.println("Downloading files...");
        Path path = serverPath.resolve(fileName);

        try {
            URLConnection connection = new URI(url).toURL().openConnection();
            connection.setRequestProperty("Accept", "application/octet-stream,*/*");
            int fileSize = connection.getContentLength();

            try (ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
                 FileOutputStream fos = new FileOutputStream(path.toFile())) {

                ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
                int bytesRead;
                int totalBytesRead = 0;

                while ((bytesRead = rbc.read(buffer)) != -1) {
                    buffer.flip();
                    fos.getChannel().write(buffer);
                    buffer.clear();

                    totalBytesRead += bytesRead;
                    if (fileSize > 0) {
                        double progress = (double) totalBytesRead / fileSize * 100;
                        progressBar.display((int) progress);
                    }
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Error downloading file", e);
        } finally {
            progressBar.reset();
        }
    }
}
