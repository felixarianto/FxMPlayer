package com.fx.app.fxmplayer;


public class Util {

    public static String toDisplay(int duration) {
        String out = null;
        long hours = 0;
        try {
            hours = (duration / 3600000);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return out;
        }
        long remaining_minutes = (duration - (hours * 3600000)) / 60000;
        String minutes = twoDigit(remaining_minutes);

        long remaining_seconds = (duration - (hours * 3600000) - (remaining_minutes * 60000));
        String seconds = twoDigit(remaining_seconds);

        if (hours > 0) {
            out = hours + ":" + minutes + ":" + seconds;
        } else {
            out = minutes + ":" + seconds;
        }

        return out;
    }

    public static int fromDisplay(String duration) {
        String[] time = duration.split(":");
        int hour   = Integer.valueOf(time[0]) * 3600000;
        int minute = Integer.valueOf(time[1]) * 60000;
        return hour + minute;
    }

    private static String twoDigit(long v) {
        if (v < 10) {
            return "0" + v;
        }
        return "" + v;
    }

}
