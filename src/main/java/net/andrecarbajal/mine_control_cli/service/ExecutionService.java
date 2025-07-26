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
public class ExecutionService {
    private ConfigurationManager configProperties;

    public void startServer(File serverPath, Path jarFilePath) {
        runProcessWithInput(
                createStartProcessBuilder(jarFilePath),
                serverPath,
                "Error starting server: "
        );
    }

    public void startForgeBasedServer(File serverDir, Path argsFilePath) {
        runProcessWithInput(
                createNeoForgeProcessBuilder(argsFilePath),
                serverDir,
                "Error starting NeoForge server: "
        );
    }

    public int startInstaller(File serverPath, Path jarFilePath) {
        try {
            ProcessBuilder processBuilder = createInstallerProcessBuilder(jarFilePath);
            processBuilder.directory(serverPath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            Thread outputThread = getOutputThread(process);
            int exitCode = process.waitFor();
            outputThread.join();
            return exitCode;
        } catch (IOException | InterruptedException e) {
            System.out.println(TextDecorationUtil.error("Error installing server: " + e.getMessage()));
            throw new RuntimeException("Error installing server: ", e);
        }
    }

    public void startPotatoPeeler(File serverPath, Path jarFilePath, String loaderType) {
        try {
            ProcessBuilder processBuilder = createPotatoPeelerProcessBuilder(jarFilePath, serverPath, loaderType);
            processBuilder.directory(serverPath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            Thread outputThread = getOutputThread(process);
            outputThread.join();
        } catch (IOException | InterruptedException e) {
            System.out.println(TextDecorationUtil.error("Error installing server: " + e.getMessage()));
            throw new RuntimeException("Error installing server: ", e);
        }
    }

    private void runProcessWithInput(ProcessBuilder processBuilder, File directory, String errorMsg) {
        try {
            processBuilder.directory(directory);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            Thread outputThread = getOutputThread(process);
            Thread inputThread = getThread(process);

            int exitCode = process.waitFor();
            outputThread.join();
            System.out.println(TextDecorationUtil.success("Server exited with code: " + exitCode));
            System.out.println(TextDecorationUtil.info("Press enter to exit..."));
            inputThread.interrupt();
            inputThread.join();
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

        String[] baseArgs = new String[]{javaPath, "-Xms" + minRam, "-Xmx" + maxRam};
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

    private ProcessBuilder createPotatoPeelerProcessBuilder(Path jarFilePath, File serverPath, String loaderType) {
        String javaPath = configProperties.getString("java.path");
        Path serverPathAsPath = serverPath.toPath();
        String chunkInhabitedTime = configProperties.getString("potato-peeler.chunk-inhabited-time");
        if (loaderType.equals("PAPER")) {
            return new ProcessBuilder(javaPath, "-jar", jarFilePath.toString(), "--min-inhabited", chunkInhabitedTime, "--world-dirs",
                    String.format("%s,%s,%s",
                            serverPathAsPath.resolve("world"),
                            serverPathAsPath.resolve("world_nether") + "/DIM-1",
                            serverPathAsPath.resolve("world_the_end") + "/DIM1"));
        } else {
            String worldFolder = serverPathAsPath.resolve("world").toString();
            return new ProcessBuilder(javaPath, "-jar", jarFilePath.toString(), "--min-inhabited", chunkInhabitedTime, "--world-dirs",
                    String.format("%s,%s,%s", worldFolder, worldFolder + "/DIM-1", worldFolder + "/DIM1"));
        }
    }
}