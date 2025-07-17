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
        runProcessWithInputOutput(
                createStartProcessBuilder(jarFilePath),
                serverPath,
                "Server exited with code: ",
                "Error starting server: ",
                true
        );
    }

    public void startForgeBasedServer(File serverDir, Path argsFilePath) {
        runProcessWithInputOutput(
                createNeoForgeProcessBuilder(argsFilePath),
                serverDir,
                "Server exited with code: ",
                "Error starting NeoForge server: ",
                true
        );
    }

    public void startInstaller(File serverPath, Path jarFilePath) {
        runProcessWithInputOutput(
                createInstallerProcessBuilder(jarFilePath),
                serverPath,
                "The installation has completed with code:",
                "Error installing server: ",
                false
        );
    }

    private void runProcessWithInputOutput(ProcessBuilder processBuilder, File directory, String successMsg, String errorMsg, boolean withInput) {
        try {
            processBuilder.directory(directory);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            Thread outputThread = getOutputThread(process);

            Thread inputThread = null;
            if (withInput) {
                inputThread = getThread(process);
            }

            int exitCode = process.waitFor();
            outputThread.join();
            System.out.println(TextDecorationUtil.success(successMsg + exitCode));
            if (withInput) {
                System.out.println(TextDecorationUtil.info("Press enter to exit..."));
                inputThread.interrupt();
                inputThread.join();
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(TextDecorationUtil.error(errorMsg + e.getMessage()));
            throw new RuntimeException(errorMsg, e);
        }
    }

    private static Thread getOutputThread(Process process) {
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
        return outputThread;
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

    private ProcessBuilder createProcessBuilder(String... args) {
        String maxRam = configProperties.getString("java.max-ram");
        String minRam = configProperties.getString("java.min-ram");
        String javaPath = configProperties.getString("java.path");
        System.out.println(TextDecorationUtil.info("Iniciando servidor con:\n" +
                "  Min RAM: " + TextDecorationUtil.cyan(minRam) + "\n" +
                "  Max RAM: " + TextDecorationUtil.cyan(maxRam) + "\n" +
                "  Java Path: " + TextDecorationUtil.green(javaPath)));

        String[] baseArgs = new String[] { javaPath, "-Xms" + minRam, "-Xmx" + maxRam };
        String[] fullArgs = new String[baseArgs.length + args.length];
        System.arraycopy(baseArgs, 0, fullArgs, 0, baseArgs.length);
        System.arraycopy(args, 0, fullArgs, baseArgs.length, args.length);
        return new ProcessBuilder(fullArgs);
    }

    private ProcessBuilder createStartProcessBuilder(Path jarFilePath) {
        return createProcessBuilder("-jar", jarFilePath.toString(), "nogui");
    }

    private ProcessBuilder createNeoForgeProcessBuilder(Path argsFilePath) {
        return createProcessBuilder("@" + argsFilePath.toString(), "nogui");
    }

    private ProcessBuilder createInstallerProcessBuilder(Path jarFilePath) {
        String javaPath = configProperties.getString("java.path");
        return new ProcessBuilder(javaPath, "-jar", jarFilePath.toString(), "--installServer");
    }
}