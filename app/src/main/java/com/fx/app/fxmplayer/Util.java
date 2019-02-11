package com.fx.app.fxmplayer;


import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

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
            else {
                return -1;
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

    public static void addFilter(final EditText edtText) {
        edtText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                lastLenght = charSequence.length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            int lastLenght = 0;
            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    String txt = edtText.getText().toString();
                    if (lastLenght < txt.length()) {
                        if (!txt.contains(":")) {
                            if(txt.length() >= 2) {
                                editable.append(":", 2, 3);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("", "", e);
                }
            }
        });
    }


}
