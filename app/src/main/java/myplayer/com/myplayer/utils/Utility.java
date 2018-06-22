package myplayer.com.myplayer.utils;

import java.util.Locale;

public class Utility {
    public static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format(Locale.US,"%d:%02d:%02d", h,m,s);
    }
}
