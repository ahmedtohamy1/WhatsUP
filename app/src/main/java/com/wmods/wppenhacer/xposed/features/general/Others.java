package com.wmods.wppenhacer.xposed.features.general;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wmods.wppenhacer.xposed.core.DesignUtils;
import com.wmods.wppenhacer.xposed.core.Feature;
import com.wmods.wppenhacer.xposed.core.ResId;
import com.wmods.wppenhacer.xposed.core.Unobfuscator;
import com.wmods.wppenhacer.xposed.core.Utils;
import com.wmods.wppenhacer.xposed.core.WppCore;
import com.wmods.wppenhacer.xposed.core.components.AlertDialogWpp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Others extends Feature {

    public static HashMap<Integer, Boolean> propsBoolean = new HashMap<>();
    public static HashMap<Integer, Integer> propsInteger = new HashMap<>();

    public Others(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {


        // Removido pois as não há necessidade de ficar em uma versão obsoleta.

//        var deprecatedMethod = Unobfuscator.loadDeprecatedMethod(loader);
//        logDebug(Unobfuscator.getMethodDescriptor(deprecatedMethod));
//
//        XposedBridge.hookMethod(deprecatedMethod, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) {
//                Date date = new Date(10554803081056L);
//                param.setResult(date);
//            }
//        });
        var novoTema = prefs.getBoolean("novotema", false);
        var menuWIcons = prefs.getBoolean("menuwicon", false);
        var newSettings = prefs.getBoolean("novaconfig", false);
        var filterChats = prefs.getString("chatfilter", null);
        var strokeButtons = prefs.getBoolean("strokebuttons", false);
        var outlinedIcons = prefs.getBoolean("outlinedicons", false);
        var showDnd = prefs.getBoolean("show_dndmode", false);
        var showFreezeLastSeen = prefs.getBoolean("show_freezeLastSeen", false);
        var removechannelRec = prefs.getBoolean("removechannel_rec", false);
        var separateGroups = prefs.getBoolean("separategroups", false);
        var filterSeen = prefs.getBoolean("filterseen", false);
        var fbstyle = prefs.getBoolean("fbstyle", false);
        var alertSticker = prefs.getBoolean("alertsticker", false);
        var channels = prefs.getBoolean("channels", false);
        var igstatus = prefs.getBoolean("igstatus", false);
        var metaai = prefs.getBoolean("metaai", false);
        var topnav = prefs.getBoolean("topnav", false);
        var videotime = prefs.getBoolean("videotime", false);

        propsBoolean.put(5171, filterSeen); // filtros de chat e grupos
        propsBoolean.put(4524, novoTema);
        propsBoolean.put(4497, menuWIcons);
        propsBoolean.put(4023, newSettings);
        propsBoolean.put(8013, Objects.equals(filterChats, "2")); // lupa sera removida e sera adicionado uma barra no lugar.
        propsBoolean.put(5834, strokeButtons);
        propsBoolean.put(5509, outlinedIcons);
        propsBoolean.put(2358, false);
        propsBoolean.put(7516, fbstyle);
        propsBoolean.put(3289, !topnav);
        propsBoolean.put(4656, !topnav);

        if (metaai) {
            propsBoolean.put(8025, false);
            propsBoolean.put(6251, false);
            propsBoolean.put(7639, false);
        }

        propsInteger.put(8522, fbstyle ? 1 : 0);
        propsInteger.put(8521, fbstyle ? 1 : 0);
        propsInteger.put(3877, channels ? igstatus ? 2 : 0 : 2);
        propsInteger.put(6728, videotime ? 60 : 30);

        var methodPropsBoolean = Unobfuscator.loadPropsBooleanMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(methodPropsBoolean));

        var dataUsageActivityClass = XposedHelpers.findClass("com.whatsapp.settings.SettingsDataUsageActivity", loader);
        var workManagerClass = Unobfuscator.loadWorkManagerClass(loader);
        XposedBridge.hookMethod(methodPropsBoolean, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int i = (int) param.args[param.args.length - 1];

                var propValue = propsBoolean.get(i);
                if (propValue != null) {
                    // Fix Bug in Settings Data Usage
                    switch (i) {
                        case 4023:
                            if (Unobfuscator.isCalledFromClass(dataUsageActivityClass))
                                return;
                            break;
                        case 3877:
                            if (!Unobfuscator.isCalledFromClass(workManagerClass))
                                return;
                            break;
                    }
                    param.setResult(propValue);
                }
            }
        });

        var methodPropsInteger = Unobfuscator.loadPropsIntegerMethod(loader);

        XposedBridge.hookMethod(methodPropsInteger, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                int i = (int) param.args[param.args.length - 1];
                var propValue = propsInteger.get(i);
                if (propValue == null) return;
                param.setResult(propValue);
            }
        });

        XposedHelpers.findAndHookMethod("com.whatsapp.HomeActivity", loader, "onCreateOptionsMenu", Menu.class, new XC_MethodHook() {
            @SuppressLint("ApplySharedPref")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Menu menu = (Menu) param.args[0];
                Activity home = (Activity) param.thisObject;
                if (prefs.getBoolean("restartbutton", true)) {
                    var iconDraw = DesignUtils.getDrawableByName("vec_account_switcher");
                    iconDraw.setTint(0xff8696a0);
                    var itemMenu = menu.add(0, 0, 0, ResId.string.restart_whatsapp).setIcon(iconDraw).setOnMenuItemClickListener(item -> {
                        restartApp(home);
                        return true;
                    });
                    if (newSettings) {
                        itemMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    }
                }
                if (showFreezeLastSeen) {
                    InsertFreezeLastSeenOption(menu, home);
                }
                if (showDnd) {
                    InsertDNDOption(menu, home);
                } else {
                    Utils.getApplication().getSharedPreferences(Utils.getApplication().getPackageName() + "_mdgwa_preferences", Context.MODE_PRIVATE).edit().putBoolean("dndmode", false).commit();
                }
            }
        });


        XposedHelpers.findAndHookMethod("com.whatsapp.HomeActivity", loader, "onPrepareOptionsMenu", Menu.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var menu = (Menu) param.args[0];
                var item = menu.findItem(Utils.getID("menuitem_search", "id"));
                if (item != null) {
                    item.setVisible(Objects.equals(filterChats, "1"));
                }
            }
        });

        if (removechannelRec) {
            var removeChannelRecClass = Unobfuscator.loadRemoveChannelRecClass(loader);
            XposedBridge.hookAllConstructors(removeChannelRecClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args.length > 0 && param.args[0] instanceof List list) {
                        if (list.isEmpty()) return;
                        list.clear();
                    }
                }
            });
        }

        if (separateGroups) {
            var filterAdaperClass = Unobfuscator.loadFilterAdaperClass(loader);
            XposedBridge.hookAllConstructors(filterAdaperClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args.length == 3 && param.args[2] instanceof List list) {
                        var newList = new ArrayList<Object>(list);
                        newList.removeIf(item -> {
                            var name = XposedHelpers.getObjectField(item, "A01");
                            return name == null || name == "CONTACTS_FILTER" || name == "GROUP_FILTER";
                        });
                        param.args[2] = newList;
                    }
                }
            });
            var methodSetFilter = Arrays.stream(filterAdaperClass.getDeclaredMethods()).filter(m -> m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(int.class)).findFirst().orElse(null);

            XposedBridge.hookMethod(methodSetFilter, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    var index = (int) param.args[0];
                    var list = (List) XposedHelpers.getObjectField(param.thisObject, "A01");
                    if (list == null || index >= list.size()) {
                        param.setResult(null);
                    }
                }
            });
        }

        if (alertSticker) {
            var sendStickerMethod = Unobfuscator.loadSendStickerMethod(loader);
            XposedBridge.hookMethod(sendStickerMethod, new XC_MethodHook() {
                private Unhook unhooked;

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    unhooked = XposedHelpers.findAndHookMethod(View.class, "setOnClickListener", View.OnClickListener.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            View.OnClickListener mCaptureOnClickListener = (View.OnClickListener) param.args[0];
                            if (mCaptureOnClickListener == null) return;
                            if (!(param.thisObject instanceof ViewGroup)) return;
                            param.args[0] = (View.OnClickListener) view -> {
                                var context = view.getContext();
                                var dialog = new AlertDialogWpp(view.getContext());
                                dialog.setTitle(context.getString(ResId.string.send_sticker));

                                var stickerView = (ImageView) ((ViewGroup) view).getChildAt(0);
                                LinearLayout linearLayout = new LinearLayout(context);
                                linearLayout.setOrientation(LinearLayout.VERTICAL);
                                linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                                var padding = Utils.dipToPixels(16);
                                linearLayout.setPadding(padding, padding, padding, padding);
                                var image = new ImageView(context);
                                var size = Utils.dipToPixels(72);
                                var params = new LinearLayout.LayoutParams(size, size);
                                params.bottomMargin = padding;
                                image.setLayoutParams(params);
                                image.setImageDrawable(stickerView.getDrawable());
                                linearLayout.addView(image);

                                TextView text = new TextView(context);
                                text.setText(context.getString(ResId.string.do_you_want_to_send_sticker));
                                text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                linearLayout.addView(text);


                                dialog.setView(linearLayout);
                                dialog.setPositiveButton(context.getString(ResId.string.send), (dialog1, which) -> mCaptureOnClickListener.onClick(view));
                                dialog.setNegativeButton(context.getString(ResId.string.cancel), null);
                                dialog.show();
                            };
                        }
                    });
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    unhooked.unhook();
                }

            });
        }
    }

    private static void restartApp(Activity home) {
        Intent intent = Utils.getApplication().getPackageManager().getLaunchIntentForPackage(Utils.getApplication().getPackageName());
        if (intent != null) {
            home.finishAffinity();
            Utils.getApplication().startActivity(intent);
        }
        Runtime.getRuntime().exit(0);
    }

    @SuppressLint({"DiscouragedApi", "UseCompatLoadingForDrawables", "ApplySharedPref"})
    private static void InsertDNDOption(Menu menu, Activity home) {
        var dndmode = WppCore.getPrivBoolean("dndmode", false);
        int iconDraw;
        iconDraw = Utils.getID(dndmode ? "ic_location_nearby_disabled" : "ic_location_nearby", "drawable");
        var item = menu.add(0, 0, 0, "Dnd Mode " + dndmode);
        item.setIcon(iconDraw);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(menuItem -> {
            if (!dndmode) {
                new AlertDialogWpp(home)
                        .setTitle(home.getString(ResId.string.dnd_mode_title))
                        .setMessage(home.getString(ResId.string.dnd_message))
                        .setPositiveButton(home.getString(ResId.string.activate), (dialog, which) -> {
                            WppCore.setPrivBoolean("dndmode", true);
                            restartApp(home);
                        })
                        .setNegativeButton(home.getString(ResId.string.cancel), (dialog, which) -> dialog.dismiss())
                        .create().show();
                return true;
            }
            WppCore.setPrivBoolean("dndmode", false);
            restartApp(home);
            return true;
        });
    }

    @SuppressLint({"DiscouragedApi", "UseCompatLoadingForDrawables", "ApplySharedPref"})
private static void InsertFreezeLastSeenOption(Menu menu, Activity home) {
    final boolean[] freezelastseen = {WppCore.getPrivBoolean("freezelastseen", false)};
    int iconDraw = Utils.getID("ic_status_receipts_disabled_shadow", "drawable");
    MenuItem item = menu.add(0, 0, 0, "Freeze Last Seen " + freezelastseen[0]);
    item.setIcon(iconDraw);
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    item.setOnMenuItemClickListener(menuItem -> {
        new AlertDialogWpp(home)
                .setTitle(home.getString(ResId.string.freezelastseen_title))
                .setMessage(home.getString(ResId.string.freezelastseen_message))
                .setPositiveButton(home.getString(ResId.string.activate), (dialog, which) -> {
                    freezelastseen[0] = !freezelastseen[0]; // Toggle the value
                    WppCore.setPrivBoolean("freezelastseen", freezelastseen[0]);
                    item.setTitle("Freeze Last Seen " + freezelastseen[0]);
                    restartApp(home);
                })
                .setNegativeButton(home.getString(ResId.string.cancel), (dialog, which) -> dialog.dismiss())
                .create().show();
        return true;
    });
}

    @NonNull
    @Override
    public String getPluginName() {
        return "Others";
    }
}
