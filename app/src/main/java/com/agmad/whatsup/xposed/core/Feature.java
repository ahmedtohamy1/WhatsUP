package com.agmad.whatsup.xposed.core;

import androidx.annotation.NonNull;


import com.agmad.whatsup.BuildConfig;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public abstract class Feature {

    public final ClassLoader loader;
    public final XSharedPreferences prefs;
    public static boolean DEBUG = false;

    public Feature(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        this.loader = loader;
        this.prefs = preferences;
    }

    public abstract void doHook() throws Throwable;

    @NonNull
    public abstract String getPluginName();

    public void logDebug(Object object) {
        if (!DEBUG) return;
        log(object);
    }

    public void log(Object object) {
        if (object instanceof Throwable) {
            XposedBridge.log(String.format("[%s] Error:", this.getPluginName()));
            XposedBridge.log((Throwable) object);
        } else {
            XposedBridge.log(String.format("[%s] %s", this.getPluginName(), object));
        }
    }
}
