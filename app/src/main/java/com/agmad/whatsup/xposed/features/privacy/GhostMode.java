package com.agmad.whatsup.xposed.features.privacy;

import androidx.annotation.NonNull;

import com.agmad.whatsup.xposed.core.Unobfuscator;
import com.agmad.whatsup.xposed.core.Feature;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class GhostMode extends Feature {

    public GhostMode(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {

        Method method = Unobfuscator.loadGhostModeMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(method));
        XposedBridge.hookMethod(method, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                var p1 = (int) param.args[2];
                var ghostmode_t = prefs.getBoolean("ghostmode_t", false);
                var ghostmode_r = prefs.getBoolean("ghostmode_r", false);
                if ((p1 == 1 && ghostmode_r) || (p1 == 0 && ghostmode_t)) {
                    param.setResult(null);
                }
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Ghost Mode";
    }
}
