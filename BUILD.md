1m=]]]]'.≤µ
## Prerequisites

- Java 21 or newer
- Maven
- (Optional) GraalVM for native builds

## Stack Overview

MineControlCli is built on a modern and robust technology stack:

* **Spring Boot 3 & Spring Shell:** Provides the core framework for our interactive CLI.
* **JLine & JNA:** Powers the rich terminal experience.
* **Apache Commons Compress:** Used for efficiently creating and restoring `.zip` backups.
* **Lombok:** Helps us keep the codebase clean and concise.

## Executing from the codebase

To run the application directly from the codebase, you can use Maven commands:

1. **To run the application:**
   ```shell
   ./mvnw spring-boot:run
   ```
2. **To run the application with a specific profile (e.g., `dev`):**
   ```shell
    ./mvnw clean spring-boot:run -Dspring-boot.run.profiles=dev
    ```

## Building the Application

To build the application, you can use the following commands:

1. **To create the executable JAR file:**
   ```shell
   ./mvnw clean -DskipTests package
   ```
2. **To create a native executable (requires GraalVM):**
   ```shell
    ./mvnw -Pnative -DskipTests native:compile
    ```

## Running the Application

To run the application after building, you can use the following commands:

1. **Using the JAR file:**
   ```shell
   java -jar target/mine-control-cli-x.x.x.jar
   ```
2. **Using the native executable:**
    - On Linux/macOS:
   ```shell
    ./target/mine-control-cli
    ```
    - On Windows:
    ```shell
    .\target\mine-control-cli.exe
    ```
