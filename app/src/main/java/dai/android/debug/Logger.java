package dai.android.debug;

import android.util.Log;

public class Logger {

    public static boolean DEBUG = true;

    public static void d(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.d(tag, msg, tr);
        }
    }

    public static void d(String tag, String msg) {
        d(tag, msg, null);
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.v(tag, msg, tr);
        }
    }

    public static void v(String tag, String msg) {
        v(tag, msg, null);
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.i(tag, msg, tr);
        }
    }

    public static void i(String tag, String msg) {
        i(tag, msg, null);
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.w(tag, msg, tr);
        }
    }

    public static void w(String tag, String msg) {
        w(tag, msg, null);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.e(tag, msg, tr);
        }
    }

    public static void e(String tag, String msg) {
        e(tag, msg, null);
    }
}
