package net.andrecarbajal.mine_control_cli.util;

import java.util.Comparator;

public class VersionComparator implements Comparator<String> {

    @Override
    public int compare(String v1, String v2) {
        String cleanV1 = cleanVersion(v1);
        String cleanV2 = cleanVersion(v2);

        String[] parts1 = cleanV1.split("\\.");
        String[] parts2 = cleanV2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int part1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int part2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (part1 != part2) {
                return Integer.compare(part2, part1);
            }
        }

        boolean isPreV1 = v1.contains("_pre") || v1.contains("-pre");
        boolean isPreV2 = v2.contains("_pre") || v2.contains("-pre");

        if (isPreV1 && !isPreV2) {
            return 1;
        } else if (!isPreV1 && isPreV2) {
            return -1;
        } else if (isPreV1 && isPreV2) {
            int preNum1 = extractPreReleaseNumber(v1);
            int preNum2 = extractPreReleaseNumber(v2);
            return Integer.compare(preNum2, preNum1);
        }

        return 0;
    }

    private String cleanVersion(String version) {
        if (version.contains("_pre")) {
            return version.substring(0, version.indexOf("_pre"));
        }
        if (version.contains("-pre")) {
            return version.substring(0, version.indexOf("-pre"));
        }
        return version;
    }

    private int extractPreReleaseNumber(String version) {
        if (version.contains("_pre")) {
            String prePart = version.substring(version.indexOf("_pre") + 4);
            return prePart.isEmpty() ? 0 : Integer.parseInt(prePart);
        }
        if (version.contains("-pre")) {
            String prePart = version.substring(version.indexOf("-pre") + 4);
            return prePart.isEmpty() ? 0 : Integer.parseInt(prePart);
        }
        return 0;
    }
}