package com.mdelsordo.rate_a_dog.util;

import android.util.Log;

import com.mdelsordo.rate_a_dog.BuildConfig;

/**
 * Created by mdelsord on 6/4/17.
 * Class that handles logging to avoid having to strip out log calls in the future.
 */

public class Logger {
    public static final boolean LOG = BuildConfig.DEBUG;

    public static void v(String tag, String msg){
        if(LOG) Log.v(tag, msg);
    }

    public static void i(String tag, String msg){
        if(LOG) Log.i(tag, msg);
    }

    public static void e(String tag, String msg){
        if(LOG) Log.e(tag, msg);
    }

    public static void d(String tag, String msg){
        if(LOG) Log.d(tag, msg);
    }

    public static void w(String tag, String msg){
        if(LOG) Log.w(tag, msg);
    }
}
