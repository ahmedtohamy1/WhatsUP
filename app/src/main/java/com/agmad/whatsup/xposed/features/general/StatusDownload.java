package com.agmad.whatsup.xposed.features.general;

import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.agmad.whatsup.xposed.core.ResId;
import com.agmad.whatsup.xposed.core.Unobfuscator;
import com.agmad.whatsup.xposed.core.Utils;
import com.agmad.whatsup.xposed.core.Feature;
import com.agmad.whatsup.xposed.utils.MimeTypeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class StatusDownload extends Feature {
    private Object messageObj;

    public StatusDownload(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }


    public void doHook() throws Exception {
        if (!prefs.getBoolean("downloadstatus", false)) return;
        var setPageActiveMethod = Unobfuscator.loadStatusActivePage(loader);
        var fieldList = Unobfuscator.getFieldByType(setPageActiveMethod.getDeclaringClass(), List.class);
        logDebug("List field: " + fieldList.getName());
        logDebug(Unobfuscator.getMethodDescriptor(setPageActiveMethod));
        var mediaClass = Unobfuscator.loadStatusDownloadMediaClass(loader);
        logDebug("Media class: " + mediaClass.getName());
        var menuStatusClass = Unobfuscator.loadMenuStatusClass(loader);
        logDebug("MenuStatus class: " + menuStatusClass.getName());
        var fieldFile = Unobfuscator.loadStatusDownloadFileField(loader);
        logDebug("File field: " + fieldFile.getName());
        var clazzSubMenu = Unobfuscator.loadStatusDownloadSubMenuClass(loader);
        logDebug("SubMenu class: " + clazzSubMenu.getName());
        var clazzMenu = Unobfuscator.loadStatusDownloadMenuClass(loader);
        logDebug("Menu class: " + clazzMenu.getName());
        var menuField = Unobfuscator.getFieldByType(clazzSubMenu, clazzMenu);
        logDebug("Menu field: " + menuField.getName());

        XposedHelpers.findAndHookMethod(menuStatusClass, "onClick", View.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (messageObj == null) return;
                var fileData = XposedHelpers.getObjectField(messageObj, "A01");
                var file = (File) XposedHelpers.getObjectField(fileData, fieldFile.getName());
                Field subMenuField = Arrays.stream(param.thisObject.getClass().getDeclaredFields()).filter(f -> f.getType() == Object.class && clazzSubMenu.isInstance(XposedHelpers.getObjectField(param.thisObject, f.getName()))).findFirst().orElse(null);
                Object submenu = XposedHelpers.getObjectField(param.thisObject, subMenuField.getName());
                var menu = (Menu) XposedHelpers.getObjectField(submenu, menuField.getName());
                if (menu.findItem(ResId.string.download) != null) return;
                menu.add(0, ResId.string.download, 0, ResId.string.download).setOnMenuItemClickListener(item -> {
                    if (copyFile(prefs, file)) {
                        Toast.makeText(Utils.getApplication(), Utils.getApplication().getString(ResId.string.saved_to) + getPathDestination(prefs, file), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Utils.getApplication(), Utils.getApplication().getString(ResId.string.error_when_saving_try_again), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
            }
        });

        XposedBridge.hookMethod(setPageActiveMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var position = (int) param.args[1];
                var list = (List<?>) XposedHelpers.getObjectField(param.args[0], fieldList.getName());
                var message = list.get(position);
                if (message != null && mediaClass.isInstance(message)) {
                    messageObj = message;
                }
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Download Status";
    }

    private boolean copyFile(SharedPreferences prefs, File file) {
        if (file == null || !file.exists()) return false;

        var destination = getPathDestination(prefs, file);

        try (FileInputStream in = new FileInputStream(file);
             FileOutputStream out = new FileOutputStream(destination)) {
            byte[] bArr = new byte[1024];
            while (true) {
                int read = in.read(bArr);
                if (read <= 0) {
                    in.close();
                    out.close();

                    MediaScannerConnection.scanFile(Utils.getApplication(),
                            new String[]{destination},
                            new String[]{MimeTypeUtils.getMimeTypeFromExtension(file.getAbsolutePath())},
                            (path, uri) -> {
                            });

                    return true;
                }
                out.write(bArr, 0, read);
            }
        } catch (IOException e) {
            XposedBridge.log(e.getMessage());
            return false;
        }
    }

    @NonNull
    private String getPathDestination(SharedPreferences sharedPreferences, @NonNull File f) {
        var fileName = f.getName().toLowerCase();

        var mediaPath = getStatusFolderPath(sharedPreferences, MimeTypeUtils.getMimeTypeFromExtension(fileName));
        if (!mediaPath.exists())
            mediaPath.mkdirs();

        return mediaPath + "/" + f.getName();
    }

    @NonNull
    private File getStatusFolderPath(SharedPreferences sharedPreferences, @NonNull String mimeType) {
        String folderPath = sharedPreferences.getString("localdownload", Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download");
        if (mimeType.contains("video")) {
            folderPath += "/WhatsApp/WhatsUP/Status Videos/";
        } else if (mimeType.contains("image")) {
            folderPath += "/WhatsApp/WhatsUP/Status Images/";
        } else if (mimeType.contains("audio")) {
            folderPath += "/WhatsApp/WhatsUP/Status Sounds/";
        } else {
            folderPath += "/WhatsApp/WhatsUP/Status Media/";
        }
        return new File(folderPath);
    }
}