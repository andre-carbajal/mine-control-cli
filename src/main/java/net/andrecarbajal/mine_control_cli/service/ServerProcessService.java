package net.andrecarbajal.mine_control_cli.service;

import lombok.AllArgsConstructor;
import net.andrecarbajal.mine_control_cli.config.ConfigurationManager;
import net.andrecarbajal.mine_control_cli.util.TextDecorationUtil;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;

@Service
@AllArgsConstructor
public class ServerProcessService {
    private ConfigurationManager configProperties;

    public void startServer(File serverPath, Path jarFilePath) {
        try {
            ProcessBuilder processBuilder = createStartProcessBuilder(jarFilePath);
            processBuilder.directory(serverPath);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    System.out.println(TextDecorationUtil.error("Error reading process output: " + e.getMessage()));
                }
            });
            outputThread.start();

            Thread inputThread = getThread(process);

            int exitCode = process.waitFor();
            System.out.println(TextDecorationUtil.success("Server exited with code: " + exitCode));
            System.out.println(TextDecorationUtil.info("Press enter to exit..."));

            inputThread.interrupt();
            inputThread.join();
        } catch (IOException | InterruptedException e) {
            System.out.println(TextDecorationUtil.error("Error starting server: " + e.getMessage()));
            throw new RuntimeException("Failed to start server process", e);
        }
    }

    public void startNeoForgeServer(File serverDir, Path argsFilePath) {
        try {
            ProcessBuilder processBuilder = createNeoForgeProcessBuilder(argsFilePath);
            processBuilder.directory(serverDir);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    System.out.println(TextDecorationUtil.error("Error reading process output: " + e.getMessage()));
                }
            });
            outputThread.start();

            Thread inputThread = getThread(process);

            int exitCode = process.waitFor();
            System.out.println(TextDecorationUtil.success("Server exited with code: " + exitCode));
            System.out.println(TextDecorationUtil.info("Press enter to exit..."));

            inputThread.interrupt();
            inputThread.join();
        } catch (IOException | InterruptedException e) {
            System.out.println(TextDecorationUtil.error("Error starting NeoForge server: " + e.getMessage()));
            throw new RuntimeException("Failed to start NeoForge server process", e);
        }
    }

    public void startInstaller(File serverPath, Path jarFilePath) {
        try {
            ProcessBuilder processBuilder = createInstallerProcessBuilder(jarFilePath);
            processBuilder.directory(serverPath);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    System.out.println(TextDecorationUtil.error("Error reading process output: " + e.getMessage()));
                }
            });
            outputThread.start();

            int exitCode = process.waitFor();
            outputThread.join();
            System.out.println(TextDecorationUtil.success("The installation has completed with code:" + exitCode));
        } catch (IOException | InterruptedException e) {
            System.out.println(TextDecorationUtil.error("Error installing server: " + e.getMessage()));
            throw new RuntimeException("Failed to install server process", e);
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
                    System.out.println(TextDecorationUtil.error("Error sending command to process: " + e.getMessage()));
                }
            }
        });
        inputThread.start();
        return inputThread;
    }

    private ProcessBuilder createStartProcessBuilder(Path jarFilePath) {
        String maxRam = configProperties.getString("java.max-ram");
        String minRam = configProperties.getString("java.min-ram");
        String javaPath = configProperties.getString("java.path");
        System.out.println(TextDecorationUtil.info("Iniciando servidor con:\n" +
                "  Min RAM: " + TextDecorationUtil.cyan(minRam) + "\n" +
                "  Max RAM: " + TextDecorationUtil.cyan(maxRam) + "\n" +
                "  Java Path: " + TextDecorationUtil.green(javaPath)));
        return new ProcessBuilder(javaPath, "-Xms" + minRam, "-Xmx" + maxRam, "-jar", jarFilePath.toString(), "nogui");
    }

    private ProcessBuilder createNeoForgeProcessBuilder(Path argsFilePath) {
        String maxRam = configProperties.getString("java.max-ram");
        String minRam = configProperties.getString("java.min-ram");
        String javaPath = configProperties.getString("java.path");
        return new ProcessBuilder(javaPath, "-Xms" + minRam, "-Xmx" + maxRam, "@" + argsFilePath.toString(), "nogui");
    }

    private ProcessBuilder createInstallerProcessBuilder(Path jarFilePath) {
        String javaPath = configProperties.getString("java.path");
        return new ProcessBuilder(javaPath, "-jar", jarFilePath.toString(), "--installServer");
    }
}