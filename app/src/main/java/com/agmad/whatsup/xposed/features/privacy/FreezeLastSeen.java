package com.agmad.whatsup.xposed.features.privacy;

import androidx.annotation.NonNull;

import com.agmad.whatsup.xposed.core.Unobfuscator;
import com.agmad.whatsup.xposed.core.Feature;
import com.agmad.whatsup.xposed.core.WppCore;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class FreezeLastSeen extends Feature {
    public FreezeLastSeen(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        if (!WppCore.getPrivBoolean("freezelastseen", false) && !prefs.getBoolean("freezelastseen", false)) return;
        var method = Unobfuscator.loadFreezeSeenMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(method));
        XposedBridge.hookMethod(method, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Freeze Last Seen";
    }


}
