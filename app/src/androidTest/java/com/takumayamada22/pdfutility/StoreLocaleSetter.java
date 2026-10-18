package com.takumayamada22.pdfutility;

import android.content.res.Configuration;
import android.os.LocaleList;
import java.lang.reflect.Method;
import java.util.Arrays;

/** Executed only by rooted Google APIs emulator app_process, never shipped in the app. */
public final class StoreLocaleSetter {
    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !Arrays.asList("ja-JP", "en-US", "ko-KR", "zh-TW", "de-DE", "fr-FR", "es-ES", "pt-BR",
                "zh-CN", "vi-VN", "fil-PH", "ne-NP", "id-ID", "it-IT").contains(args[0])) {
            throw new IllegalArgumentException("Unsupported capture locale");
        }
        Method getService = Class.forName("android.app.ActivityManager").getDeclaredMethod("getService");
        getService.setAccessible(true);
        Object service = getService.invoke(null);
        Class<?> api = Class.forName("android.app.IActivityManager");
        Configuration config = new Configuration((Configuration) api.getMethod("getConfiguration").invoke(service));
        config.setLocales(LocaleList.forLanguageTags(args[0]));
        Configuration.class.getField("userSetLocale").setBoolean(config, true);
        api.getMethod("updatePersistentConfiguration", Configuration.class).invoke(service, config);
        System.out.println("System locale updated: " + args[0]);
    }
}
