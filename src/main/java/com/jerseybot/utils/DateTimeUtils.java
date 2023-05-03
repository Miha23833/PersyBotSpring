package com.jerseybot.utils;

public interface DateTimeUtils {
    static String toTimeDuration(Long duration) {
        long second = (duration / 1000) % 60;
        long minute = (duration / (1000 * 60)) % 60;
        long hour = (duration / (1000 * 60 * 60)) % 24;

        return getDuration(hour) + ":" + getDuration(minute) + ":" + getDuration(second);
    }

    static private String getDuration(long duration) {
        if (duration < 10) {
            return "0"+duration;
        }
        return String.valueOf(duration);
    }
}
