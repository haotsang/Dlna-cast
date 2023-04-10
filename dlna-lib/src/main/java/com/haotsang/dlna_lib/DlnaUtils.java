package com.haotsang.dlna_lib;

public class DlnaUtils {
    public static String formatTimeSeconds(int seconds) {
        if (seconds < 10) {
            return String.format("00:0%s", seconds);
        }
        if (seconds < 60) {
            return String.format("00:%s", seconds);
        }
        int minutes = seconds / 60;
        int secs = seconds % 60;
        if (minutes < 10) {
            if (secs < 10) {
                return String.format("0%s:0%s", minutes, secs);
            }
            return String.format("0%s:%s", minutes, secs);
        } else {
            return String.format("%s:%s", minutes, secs);
        }
    }
}
