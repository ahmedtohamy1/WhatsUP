package com.agmad.whatsup.xposed.features.general;

import android.content.Context;

import androidx.annotation.NonNull;

import com.agmad.whatsup.xposed.core.Unobfuscator;
import com.agmad.whatsup.xposed.core.Utils;
import com.agmad.whatsup.xposed.core.Feature;
import com.agmad.whatsup.xposed.core.WppCore;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class DndMode extends Feature {
    public DndMode(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        if (!WppCore.getPrivBoolean("dndmode",false)) return;
        var dndMethod = Unobfuscator.loadDndModeMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(dndMethod));
        XposedBridge.hookMethod(dndMethod, XC_MethodReplacement.DO_NOTHING);
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Dnd Mode";
    }
}
