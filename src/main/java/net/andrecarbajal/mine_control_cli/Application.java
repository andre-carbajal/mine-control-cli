package net.andrecarbajal.mine_control_cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@CommandScan
@SpringBootApplication
public class Application {
    public static final String APP_FOLDER_NAME = "mine-control-cli";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
