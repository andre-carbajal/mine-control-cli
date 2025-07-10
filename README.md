# MineControlCli: Your Ultimate Minecraft Server Manager

Tired of the complex and repetitive process of setting up and managing Minecraft servers? Meet **MineControlCli**, a powerful and intuitive command-line tool designed to streamline every aspect of server administration. From creating a brand-new Paper or Fabric server to managing backups and configurations, MineControlCli handles the heavy lifting so you can focus on playing.

This tool is built for server owners, developers, and anyone who wants a faster, more efficient way to manage Minecraft servers directly from the terminal.

## Project Requirements

To run MineControlCli, you'll need the following on your system:

*   **Java 21 or newer:** Required to execute the `.jar` version of the application.
*   **Maven (Optional):** Needed only if you plan to build the project from the source code.

For convenience, we also provide pre-compiled native executables in our releases. These have no external Java dependency and offer faster startup times.

## Dependencies

MineControlCli is built on a modern and robust technology stack:

*   **Spring Boot 3 & Spring Shell:** Provides the core framework for our interactive CLI.
*   **JLine & JNA:** Powers the rich terminal experience.
*   **Apache Commons Compress:** Used for efficiently creating and restoring `.zip` backups.
*   **Lombok:** Helps us keep the codebase clean and concise.

## Getting Started

### Installation

The recommended way to get started is by downloading the latest pre-compiled binary from the [GitHub Releases page](https://github.com/MineControlCli/mine-control-cli/releases). You can choose between the executable `.jar` file or a native executable for your operating system (Windows, macOS, Linux).

### Building from Source

If you prefer to build the project yourself, you can compile it with Maven:

1.  **To create the executable JAR file:**
    ```shell
    mvn clean package
    ```
2.  **To create a native executable (requires GraalVM):**
    ```shell
    mvn -Pnative native:compile
    ```

### First Run

When you run MineControlCli for the first time, it will automatically create a configuration directory and a `minecontrol.properties` file in your system's standard application data location. This file holds all your settings, such as memory allocation for servers and default file paths.

## How to run the application

Once you have the application, launch it from your terminal.

*   **Using the JAR file:**
    ```shell
    java -jar mine-control-cli-2.0.0.jar
    ```

*   **Using the native executable:**
    ```shell
    # On Linux/macOS
    ./mine-control-cli
    
    # On Windows
    .\mine-control-cli.exe
    ```

Executing the command will launch the interactive shell, where you can start managing your servers.

```
minecontrol:>
```

Type `help` to see a full list of available commands.

## Relevant Code Examples

Here are some examples of the most common commands to get you up and running.

### Creating a New Server

The `server create` command (alias `sc`) walks you through creating a server. If you run it without any options, it will interactively prompt you for all the necessary information.

```shell
minecontrol:> server create
```

This will trigger a series of prompts to select a server name, loader type (like Vanilla, Paper, or Fabric), and the desired game version.

You can also provide all the details as command-line options for a non-interactive setup.

```shell
# Create a Paper server for Minecraft 1.20.4
minecontrol:> server create --serverName MyPaperMC --loaderType PAPER --minecraftVersion 1.20.4
```

### Managing Your Servers

Once you have a few servers, managing them is straightforward.

To **list all available servers**, use the `server list` command (alias `sl`):

```shell
minecontrol:> server list
```

**Output:**
```
=== Available Servers ===
   1. MyPaperMC
   2. FabricFun
=========================
```

To **start a server**, use the `server start` command (alias `ss`) with the server's name.

```shell
minecontrol:> server start --serverName MyPaperMC
```

The application will locate the server, apply the configured Java settings (like RAM), and launch the server process. All server output will be displayed directly in your terminal.

### Backing Up and Restoring

Protecting your server worlds is critical. MineControlCli makes backups a breeze.

To **create a backup**, use the `backup create` command. It will prompt you to select which server to back up.

```shell
minecontrol:> backup create
```
The tool will compress the entire server directory into a `.zip` file and show a progress bar while it works.

To **restore a server from a backup**, use the `backup restore` command. You'll be asked to choose a backup file and a target server. **Note:** This action will overwrite the target server's current files.

```shell
minecontrol:> backup restore
```

### Managing Configuration

You can view and modify the application's settings without ever leaving the CLI.

To **list all current configuration properties**, use `config list`:

```shell
minecontrol:> config list
```
**Output:**
```
Configuration properties:
java.max-ram = 4096M
java.min-ram = 2048M
paths.servers = /home/user/.MineControlCli/servers
...
```

To **change a setting**, like the maximum RAM for your servers, use `config set`.

```shell
minecontrol:> config set java.max-ram 8192M
```
**Output:**
```
Configuration property 'java.max-ram' updated from '4096M' to '8192M'.
```

## Conclusion

MineControlCli is your one-stop shop for Minecraft server management from the command line. It's designed to be simple, powerful, and adaptable to your needs.

We are always looking for ways to improve. If you encounter a bug, have a feature idea, or want to contribute, please feel free to [open an issue](https://github.com/MineControlCli/mine-control-cli/issues) on our GitHub repository. Happy crafting