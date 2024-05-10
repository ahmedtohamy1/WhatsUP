package com.wmods.wppenhacer.xposed.features.customization;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView; // Added for displaying last seen time

import androidx.annotation.NonNull;

import com.wmods.wppenhacer.xposed.core.Unobfuscator;
import com.wmods.wppenhacer.xposed.core.UnobfuscatorCache;
import com.wmods.wppenhacer.xposed.core.Utils;
import com.wmods.wppenhacer.xposed.core.WppCore;
import com.wmods.wppenhacer.xposed.core.Feature;

import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class DotOnline extends Feature {

    public static HashMap<Object, View> views = new HashMap<>();
    private Object mStatusUser;
    private Object mInstancePresence;

    public DotOnline(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {
        if (!prefs.getBoolean("dotonline", false)) return;

        var classViewHolder = XposedHelpers.findClass("com.whatsapp.conversationslist.ViewHolder", loader);
        XposedBridge.hookAllConstructors(classViewHolder, new XC_MethodHook() {
            @SuppressLint("ResourceType")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var view = (View) param.args[1];
                var context = (Context) param.args[0];
                views.remove(param.thisObject);
                views.put(param.thisObject, view);
                var bottomLayout = (LinearLayout) view.findViewById(Utils.getID("bottom_row", "id"));
                var imageView = new ImageView(context);
                imageView.setId(0x7FFF0001);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
                imageView.setForegroundGravity(Gravity.CENTER_VERTICAL);
                ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
                shapeDrawable.getPaint().setColor(Color.GREEN);
                // Define the size of the circle
                shapeDrawable.setIntrinsicHeight(20);
                shapeDrawable.setIntrinsicWidth(20);
                imageView.setImageDrawable(shapeDrawable);
                imageView.setVisibility(View.GONE);
                bottomLayout.addView(imageView);

                // Add TextView to show last seen time
                TextView lastSeenText = new TextView(context);
                lastSeenText.setId(0x7FFF0002);
                lastSeenText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
                lastSeenText.setGravity(Gravity.CENTER_VERTICAL);
                lastSeenText.setVisibility(View.GONE);
                bottomLayout.addView(lastSeenText);
            }
        });

        var onChangeStatus = Unobfuscator.loadOnChangeStatus(loader);
        logDebug(Unobfuscator.getMethodDescriptor(onChangeStatus));
        var field1 = Unobfuscator.loadViewHolderField1(loader);
        logDebug(Unobfuscator.getFieldDescriptor(field1));
        var getStatusUser = Unobfuscator.loadGetStatusUserMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(getStatusUser));
        var sendPresenceMethod = Unobfuscator.loadSendPresenceMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(sendPresenceMethod));


        XposedBridge.hookAllConstructors(getStatusUser.getDeclaringClass(), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mStatusUser = param.thisObject;
            }
        });

        XposedBridge.hookAllConstructors(sendPresenceMethod.getDeclaringClass(), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mInstancePresence = param.thisObject;
            }
        });

        XposedBridge.hookMethod(onChangeStatus, new XC_MethodHook() {
            @Override
            @SuppressLint("ResourceType")
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var viewHolder = field1.get(field1.getDeclaringClass().cast(param.thisObject));
                var object = param.args[0];
                var view = (View) views.get(viewHolder);
                var csDot = (ImageView) view.findViewById(0x7FFF0001);
                var lastSeenText = (TextView) view.findViewById(0x7FFF0002); // Get the TextView for last seen time
                csDot.setVisibility(View.GONE);
                lastSeenText.setVisibility(View.GONE); // Hide last seen time initially
                var jidFiled = Unobfuscator.getFieldByExtendType(object.getClass(), XposedHelpers.findClass("com.whatsapp.jid.Jid", loader));
                var jidObject = jidFiled.get(object);
                var jid = WppCore.getRawString(jidObject);
                if (jid.contains("@g.us")) return;
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        var clazz = sendPresenceMethod.getParameterTypes()[1];
                        var instance = XposedHelpers.newInstance(clazz, new Object[]{null, null});
                        sendPresenceMethod.invoke(null, jidObject, instance, mInstancePresence);
                        var status = (String) getStatusUser.invoke(mStatusUser, object);
                        if (!TextUtils.isEmpty(status) && status.trim().equals(UnobfuscatorCache.getInstance().getString("online"))) {
                            csDot.setVisibility(View.VISIBLE);
                        } else if (!TextUtils.isEmpty(status) ) {
                            // If not online, show last seen time
                         // Logic to get last seen time goes here
                            lastSeenText.setText(status);
                            lastSeenText.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        logDebug(e);
                    }
                });
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Conversation";
    }
}
