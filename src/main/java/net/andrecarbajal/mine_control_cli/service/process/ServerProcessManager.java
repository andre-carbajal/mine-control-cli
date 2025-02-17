package net.andrecarbajal.mine_control_cli.service.process;

import lombok.AllArgsConstructor;
import net.andrecarbajal.mine_control_cli.exception.ServerStartException;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Properties;

@Service
@AllArgsConstructor
public class ServerProcessManager {
    private Properties minecraftProperties;

    public void startServer(Path serverPath, Path jarFilePath) throws ServerStartException {
        try {
            ProcessBuilder processBuilder = createProcessBuilder(jarFilePath);
            processBuilder.directory(serverPath.toFile());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    System.out.println("Error reading process output: " + e.getMessage());
                }
            });
            outputThread.start();

            Thread inputThread = getThread(process);

            int exitCode = process.waitFor();
            System.out.printf("Server exited with code: %s \nPress enter to exit", exitCode);

            inputThread.interrupt();
            inputThread.join();
        } catch (IOException | InterruptedException e) {
            throw new ServerStartException("Failed to start server", e);
        }
    }

    private static Thread getThread(Process process) {
        Thread inputThread = new Thread(() -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                String command;
                while (process.isAlive() && (command = consoleReader.readLine()) != null) {
                    writer.write(command);
                    writer.newLine();
                    writer.flush();
                }
            } catch (IOException e) {
                if (process.isAlive()) {
                    System.out.println("Error sending command to process: " + e.getMessage());
                }
            }
        });
        inputThread.start();
        return inputThread;
    }

    private ProcessBuilder createProcessBuilder(Path jarFilePath) {
        var ram = minecraftProperties.getProperty("server.ram");
        System.out.println("Executing server with ram: " + ram);
        var javaPath = minecraftProperties.getProperty("java.path");
        System.out.println("Using java path: " + javaPath);
        return new ProcessBuilder(javaPath, "-Xmx" + ram, "-jar", jarFilePath.toString(), "nogui");
    }
}