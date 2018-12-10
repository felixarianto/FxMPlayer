package com.fx.app.fxmplayer;


import android.util.Log;

public class Util {

    public static String toDisplay(int duration) {
        String out = null;
        long hours = 0;
        try {
            hours = (duration / 3600000);
        } catch (Exception e) {
            Log.e("Util.toDisplay", "", e);
            return out;
        }
        long remaining_minutes = (duration - (hours * 3600000)) / 60000;
        String minutes = twoDigit(remaining_minutes);

        long remaining_seconds = (duration - (hours * 3600000) - (remaining_minutes * 60000)) / 1000;
        String seconds = twoDigit(remaining_seconds);

        if (hours > 0) {
            out = hours + ":" + minutes + ":" + seconds;
        } else {
            out = minutes + ":" + seconds;
        }

        return out;
    }

    public static int fromDisplay(String duration) {
        try {
            int hour=0, minute=0, second=0;
            String[] time = duration.split(":");
            if (time.length == 3) {
                hour   = Integer.valueOf(time[0]) * 3600000;
                minute = Integer.valueOf(time[1]) * 60000;
                second = Integer.valueOf(time[2]) * 1000;
            }
            else if (time.length == 2) {
                minute = Integer.valueOf(time[0]) * 60000;
                second = Integer.valueOf(time[1]) * 1000;
            }
            return hour + minute + second;
        } catch (Exception e) {
            Log.e("Util.fromDisplay", "", e);
        }
        return -1;
    }

    private static String twoDigit(long v) {
        if (v < 10) {
            return "0" + v;
        }
        return "" + v;
    }



}
