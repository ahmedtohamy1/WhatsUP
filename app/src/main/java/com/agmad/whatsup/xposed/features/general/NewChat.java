package com.agmad.whatsup.xposed.features.general;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.agmad.whatsup.xposed.core.Feature;
import com.agmad.whatsup.xposed.core.ResId;
import com.agmad.whatsup.xposed.core.Utils;
import com.agmad.whatsup.xposed.core.components.AlertDialogWpp;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;

public class NewChat extends Feature {
    public NewChat(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() {
        var homeActivity = findClass("com.whatsapp.HomeActivity", loader);
        var newSettings = prefs.getBoolean("novaconfig", false);
        if (!prefs.getBoolean("newchat", true))return;

        findAndHookMethod(homeActivity, "onCreateOptionsMenu", Menu.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var home = (Activity) param.thisObject;
                var menu = (Menu) param.args[0];

                var item = menu.add(0, 0, 0, ResId.string.new_chat);
                item.setIcon(Utils.getID("vec_ic_chat_add", "drawable"));
                if (newSettings) {
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                }
                item.setOnMenuItemClickListener(item1 -> {
                    var view = new LinearLayout(home);
                    view.setGravity(Gravity.CENTER);
                    view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    var edt = new EditText(view.getContext());
                    edt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
                    edt.setMaxLines(1);
                    edt.setInputType(InputType.TYPE_CLASS_PHONE);
                    edt.setTransformationMethod(null);
                    edt.setHint(ResId.string.number_with_country_code);
                    view.addView(edt);
                    new AlertDialogWpp(home)
                            .setTitle(home.getString(ResId.string.new_chat))
                            .setView(view)
                            .setPositiveButton(home.getString(ResId.string.message), (dialog, which) -> {
                                var number = edt.getText().toString();
                                var numberFomatted = number.replaceAll("[+\\-()/\\s]", "");
                                var intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("https://wa.me/" + numberFomatted));
                                intent.setPackage(Utils.getApplication().getPackageName());
                                home.startActivity(intent);
                            })
                            .setNegativeButton(home.getString(ResId.string.cancel), null)
                            .show();
                    return true;
                });

                super.afterHookedMethod(param);
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "New Chat";
    }
}
