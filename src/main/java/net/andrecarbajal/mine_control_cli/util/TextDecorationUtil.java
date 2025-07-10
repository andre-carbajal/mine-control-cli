package net.andrecarbajal.mine_control_cli.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TextDecorationUtil {
    private final String RESET = "\u001B[0m";
    private final String RED = "\u001B[31m";
    private final String GREEN = "\u001B[32m";
    private final String YELLOW = "\u001B[33m";
    private final String CYAN = "\u001B[36m";

    public String error(String text) {
        return RED + "[ERROR] " + RESET + text;
    }

    public String info(String text) {
        return YELLOW + "[INFO] " + RESET + text;
    }

    public String success(String text) {
        return GREEN + text + RESET;
    }

    public String cyan(String text) {
        return CYAN + text + RESET;
    }

    public String green(String text) {
        return GREEN + text + RESET;
    }
}
