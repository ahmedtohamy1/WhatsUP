package com.agmad.whatsup.xposed.features.privacy;

import android.view.View;

import androidx.annotation.NonNull;

import com.agmad.whatsup.xposed.core.Unobfuscator;
import com.agmad.whatsup.xposed.core.Feature;

import java.util.HashSet;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class HideArchive extends Feature {

    public static final HashSet<View.OnClickListener> mClickListenerList = new HashSet<>();

    public HideArchive(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("hidearchive", false))
            return;
        var archiveHideViewMethod = Unobfuscator.loadArchiveHideViewMethod(loader);
        for (var method : archiveHideViewMethod) {
            logDebug(Unobfuscator.getMethodDescriptor(method));
            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = false;
                }
            });
        }
        var onclickCapture = Unobfuscator.loadArchiveOnclickCaptureMethod(loader);
        for (var method : onclickCapture) {
            logDebug(Unobfuscator.getMethodDescriptor(method));
            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    mClickListenerList.add((View.OnClickListener) param.args[0]);
                }
            });
        }


    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Hide Archive";
    }
}
