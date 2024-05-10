package com.agmad.whatsup.xposed.features.general;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;

import com.agmad.whatsup.adapter.MessageAdapter;
import com.agmad.whatsup.views.NoScrollListView;
import com.agmad.whatsup.xposed.core.DesignUtils;
import com.agmad.whatsup.xposed.core.ResId;
import com.agmad.whatsup.xposed.core.Unobfuscator;
import com.agmad.whatsup.xposed.core.Utils;
import com.agmad.whatsup.xposed.core.WppCore;
import com.agmad.whatsup.xposed.core.Feature;
import com.agmad.whatsup.xposed.core.db.MessageHistory;
import com.agmad.whatsup.xposed.core.db.MessageStore;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ShowEditMessage extends Feature {
    private Object mConversation;

    public ShowEditMessage(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {

        var onStartMethod = Unobfuscator.loadAntiRevokeOnStartMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(onStartMethod));

        var onResumeMethod = Unobfuscator.loadAntiRevokeOnResumeMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(onResumeMethod));

        var onMessageEdit = Unobfuscator.loadMessageEditMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(onMessageEdit));

        var getEditMessage = Unobfuscator.loadGetEditMessageMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(getEditMessage));

        var getFieldIdMessage = Unobfuscator.loadSetEditMessageField(loader);
        logDebug(Unobfuscator.getFieldDescriptor(getFieldIdMessage));

        var newMessageMethod = Unobfuscator.loadNewMessageMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(newMessageMethod));

        var newMessageWithMediaMethod = Unobfuscator.loadNewMessageWithMediaMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(newMessageMethod));

        var editMessageShowMethod = Unobfuscator.loadEditMessageShowMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(editMessageShowMethod));

        var editMessageViewField = Unobfuscator.loadEditMessageViewField(loader);
        logDebug(Unobfuscator.getFieldDescriptor(editMessageViewField));


        XposedBridge.hookMethod(onStartMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                mConversation = param.thisObject;
            }
        });

        XposedBridge.hookMethod(onResumeMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                mConversation = param.thisObject;
            }
        });

        XposedBridge.hookMethod(onMessageEdit, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                var editMessage = getEditMessage.invoke(param.args[0]);
                if (editMessage == null) return;
                long timestamp = XposedHelpers.getLongField(editMessage, "A00");
                if (timestamp == 0L) return;
                long id = getFieldIdMessage.getLong(param.args[0]);
                String newMessage = (String) newMessageMethod.invoke(param.args[0]);
                if (newMessage == null){
                    newMessage = (String) newMessageWithMediaMethod.invoke(param.args[0]);
                }
                try {
                    MessageHistory.getInstance(Utils.getApplication()).insertMessage(id, newMessage, timestamp);
                } catch (Exception e) {
                    logDebug(e);
                }
            }
        });


        XposedBridge.hookMethod(editMessageShowMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var textView = (TextView) editMessageViewField.get(param.thisObject);
                if (textView != null && !textView.getText().toString().contains("\uD83D\uDCDD")) {
                    textView.getPaint().setUnderlineText(true);
                    textView.append("\uD83D\uDCDD");
                    textView.setOnClickListener((v) -> {
                        try {
                            var messageObj = XposedHelpers.callMethod(param.thisObject, "getFMessage");
                            long id = getFieldIdMessage.getLong(messageObj);
                            var msg = new MessageHistory.MessageItem(id, MessageStore.getMessageById(id), 0);
                            var messages = MessageHistory.getInstance(Utils.getApplication()).getMessages(id);
                            if (messages == null) {
                                messages = new ArrayList<>();
                            } else {
                                messages.add(0, msg);
                            }
                            showBottomDialog(messages);
                        } catch (Exception exception0) {
                            logDebug(exception0);
                        }
                    });
                }
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void showBottomDialog(ArrayList<MessageHistory.MessageItem> messages) {
        ((Activity) mConversation).runOnUiThread(() -> {
            var ctx = (Context) mConversation;

            var dialog = WppCore.createDialog(ctx);
            // NestedScrollView
            NestedScrollView nestedScrollView0 = new NestedScrollView(ctx, null);
            nestedScrollView0.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            nestedScrollView0.setFillViewport(true);
            nestedScrollView0.setFitsSystemWindows(true);
            // Main Layout
            LinearLayout linearLayout = new LinearLayout(ctx);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            linearLayout.setFitsSystemWindows(true);
            linearLayout.setMinimumHeight(layoutParams.height = Utils.getApplication().getResources().getDisplayMetrics().heightPixels / 4);
            linearLayout.setLayoutParams(layoutParams);
            int dip = Utils.dipToPixels(20);
            linearLayout.setPadding(dip, dip, dip, 0);
            Drawable bg = DesignUtils.createDrawable("rc_dialog_bg");
            if (bg != null) {
                bg.setTint(DesignUtils.getPrimarySurfaceColor(ctx));
            }
            linearLayout.setBackground(bg);

            // Title View
            TextView titleView = new TextView(ctx);
            LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams1.weight = 1.0f;
            layoutParams1.setMargins(0, 0, 0, Utils.dipToPixels(10));
            titleView.setLayoutParams(layoutParams1);
            titleView.setTextSize(16.0f);
            titleView.setTextColor(DesignUtils.getPrimaryTextColor(ctx));
            titleView.setTypeface(null, Typeface.BOLD);
            titleView.setText(ResId.string.edited_history);

            // List View
            var adapter = new MessageAdapter(ctx, messages);
            ListView listView = new NoScrollListView(ctx);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams2.weight = 1.0f;
            listView.setLayoutParams(layoutParams2);
            listView.setAdapter(adapter);
            ImageView imageView0 = new ImageView(ctx);
            LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(Utils.dipToPixels(70), Utils.dipToPixels(8));
            layoutParams4.gravity = 17;
            layoutParams4.setMargins(0, Utils.dipToPixels(5), 0, Utils.dipToPixels(5));
            var bg2 = DesignUtils.createDrawable("rc_dotline_dialog");
            imageView0.setBackground(DesignUtils.alphaDrawable(bg2, DesignUtils.getPrimaryTextColor(ctx), 33));
            imageView0.setLayoutParams(layoutParams4);
            // Button View
            Button okButton = new Button(ctx);
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams3.setMargins(0, Utils.dipToPixels(10), 0, Utils.dipToPixels(10));
            layoutParams3.gravity = 80;
            okButton.setLayoutParams(layoutParams3);
            okButton.setGravity(17);
            var drawable = DesignUtils.createDrawable("selector_bg");
            okButton.setBackground(DesignUtils.alphaDrawable(drawable, DesignUtils.getPrimaryTextColor(ctx), 25));
            okButton.setText("OK");
            okButton.setOnClickListener((View view) -> dialog.dismiss());
            linearLayout.addView(imageView0);
            linearLayout.addView(titleView);
            linearLayout.addView(listView);
            linearLayout.addView(okButton);
            nestedScrollView0.addView(linearLayout);
            dialog.setContentView(nestedScrollView0);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        });
    }


    @NonNull
    @Override
    public String getPluginName() {
        return "Anti Edit Message";
    }

}
