# PDFBox-Android rules (allow R8 obfuscation and optimization)
-keep,allowobfuscation,allowoptimization class com.tom_roush.pdfbox.** { *; }
-keep,allowobfuscation,allowoptimization class org.apache.fontbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**
-dontwarn org.apache.fontbox.**

# Google Play Billing rules (allow obfuscation and optimization)
-keep,allowobfuscation,allowoptimization class com.android.billingclient.api.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}

# Support multi-language resources
-keep class **.R$* {
    <fields>;
}
