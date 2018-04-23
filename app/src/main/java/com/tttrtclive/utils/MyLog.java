package com.tttrtclive.utils;

import android.util.Log;

public class MyLog {
    private static boolean IS_DEBUG = true;
    public static final String TAG = "WSTECH";

    public static void i(String msg) {
        if (!IS_DEBUG) {
            return;
        }
        Log.i(TAG, msg);
    }

    public static void d(String msg) {
        if (!IS_DEBUG) {
            return;
        }
        Log.d(TAG, msg);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (!IS_DEBUG) {
            return;
        }
        Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (!IS_DEBUG) {
            return;
        }
        Log.d(tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }
}
