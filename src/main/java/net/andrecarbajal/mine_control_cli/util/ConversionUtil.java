package net.andrecarbajal.mine_control_cli.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ConversionUtil {
    public String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
