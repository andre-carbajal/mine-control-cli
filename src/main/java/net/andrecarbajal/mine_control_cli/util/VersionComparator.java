package net.andrecarbajal.mine_control_cli.util;

import java.util.Comparator;

public class VersionComparator implements Comparator<String> {

    @Override
    public int compare(String v1, String v2) {
        if (v1 == null && v2 == null) return 0;
        if (v1 == null) return 1;
        if (v2 == null) return -1;

        v1 = v1.trim();
        v2 = v2.trim();

        String cleanV1 = cleanVersion(v1);
        String cleanV2 = cleanVersion(v2);

        String[] parts1 = cleanV1.split("\\.");
        String[] parts2 = cleanV2.split("\\.");
        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int part1 = i < parts1.length ? parseIntSafe(parts1[i]) : 0;
            int part2 = i < parts2.length ? parseIntSafe(parts2[i]) : 0;
            if (part1 != part2) {
                return Integer.compare(part1, part2);
            }
        }

        boolean isPreV1 = isPreRelease(v1);
        boolean isPreV2 = isPreRelease(v2);

        if (isPreV1 && !isPreV2) {
            return -1;
        }
        if (!isPreV1 && isPreV2) {
            return 1;
        }
        if (isPreV1 && isPreV2) {
            int preNum1 = extractPreReleaseNumber(v1);
            int preNum2 = extractPreReleaseNumber(v2);
            return Integer.compare(preNum1, preNum2);
        }
        return 0;
    }

    private boolean isPreRelease(String version) {
        return version != null && (version.contains("_pre") || version.contains("-pre"));
    }

    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String cleanVersion(String version) {
        if (version == null) return "";
        version = version.trim();
        if (version.contains("_pre")) {
            return version.substring(0, version.indexOf("_pre"));
        }
        if (version.contains("-pre")) {
            return version.substring(0, version.indexOf("-pre"));
        }
        return version;
    }

    private int extractPreReleaseNumber(String version) {
        if (version == null) return 0;

        try {
            if (version.contains("_pre")) {
                String prePart = version.substring(version.indexOf("_pre") + 4);
                return prePart.isEmpty() ? 0 : Integer.parseInt(prePart.trim());
            }
            if (version.contains("-pre")) {
                String prePart = version.substring(version.indexOf("-pre") + 4);
                return prePart.isEmpty() ? 0 : Integer.parseInt(prePart.trim());
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        return 0;
    }
}