package com.agmad.whatsup.xposed.features.privacy;

import androidx.annotation.NonNull;

import com.agmad.whatsup.xposed.core.Unobfuscator;
import com.agmad.whatsup.xposed.core.Feature;
import com.agmad.whatsup.xposed.core.WppCore;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class HideReceipt extends Feature {
    public HideReceipt(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {
        var method = Unobfuscator.loadReceiptMethod(loader);
        var method2 = Unobfuscator.loadReceiptMethod2(loader);
        logDebug(Unobfuscator.getMethodDescriptor(method));
        XposedBridge.hookMethod(method, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!prefs.getBoolean("hidereceipt", false)) return;
                if (!Unobfuscator.isCalledFromMethod(method2)) return;
                var jid = WppCore.getRawString(param.args[0]);
                if ((jid == null || jid.contains("@lid")) && param.args[4] != "sender") {
                    param.args[4] = "inactive";
                }
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Hide Receipt";
    }
}
