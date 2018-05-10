
package com.mediatek.wwtv.util;

import android.util.Log;
import java.io.File;

/**
 *
 * @author mtk40530
 *
 */
public final class MtkLog {
    public static boolean logOnFlag = false;
    private static final String LOG = "[MtkMMPTVLog] ";

    private MtkLog() {
    }

    static {
        init();
    }

    private static void init() {
        File file = new File("/data/tkui.print");

        if (file.exists()) {
            logOnFlag = true;
            Log.i(MtkLog.class.getSimpleName(), LOG + "print log");
            return;
        }

        Log.i(MtkLog.class.getSimpleName(), LOG + "not print log");
    }

    public static void v(String tag, String msg) {
        if (logOnFlag) {
            Log.v(tag, LOG + msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (logOnFlag) {
            Log.v(tag, LOG + msg, tr);
        }
    }

    public static void d(String tag, String msg) {
        if (logOnFlag) {
            Log.d(tag, LOG + msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (logOnFlag) {
            Log.d(tag, LOG + msg, tr);
        }
    }

    public static void i(String tag, String msg) {
        if (logOnFlag) {
            Log.i(tag, LOG + msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (logOnFlag) {
            Log.i(tag, LOG + msg, tr);
        }
    }

    public static void w(String tag, String msg) {
        if (logOnFlag) {
            Log.w(tag, LOG + msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (logOnFlag) {
            Log.w(tag, LOG + msg, tr);
        }
    }

    public static void e(String tag, String msg) {
        if (logOnFlag) {
            Log.e(tag, LOG + msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (logOnFlag) {
            Log.e(tag, LOG + msg, tr);
        }
    }

    public static void printStackTrace() {
        Throwable tr = new Throwable();
        Log.getStackTraceString(tr);
        tr.printStackTrace();
    }

    static int loglevel = -1;

    public static void printf(String info) {
        Throwable tr = new Throwable();
        StackTraceElement[] elems = tr.getStackTrace();

        if (loglevel == -1) {
            if ((new java.io.File("/data/printAll")).exists()) {
                loglevel = 9;
            }
            else if ((new java.io.File("/data/printMiddle")).exists()) {
                loglevel = 5;
            }
            else {
                loglevel = 0;
            }
        }

        if (0 == loglevel) {
            if (elems != null && elems.length > 1) {
                Log.e(elems[1].getFileName(), elems[1].getMethodName() +
                        "," + elems[1].getLineNumber() + ". " + info);
            }
            else {
                Log.getStackTraceString(tr);
                tr.printStackTrace();
            }
        }
        else if (5 == loglevel) {
            if (elems != null && elems.length > 2) {
                Log.e(elems[1].getFileName(), elems[1].getMethodName() +
                        "," + elems[1].getLineNumber() + ". " + info);
                Log.e(elems[2].getFileName(), elems[2].getMethodName() +
                        "," + elems[2].getLineNumber() + ". " + info);
            }
            else {
                Log.getStackTraceString(tr);
                tr.printStackTrace();
            }
        }
        else {
            Log.getStackTraceString(tr);
            tr.printStackTrace();
        }
    }
}
