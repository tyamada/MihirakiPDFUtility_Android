# PDFBox-Android rules
-keep class com.tom_roush.pdfbox.** { *; }
-keep class org.apache.fontbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**
-dontwarn org.apache.fontbox.**

# Google Play Billing rules
-keep class com.android.billingclient.api.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}

# Support multi-language resources
-keep class **.R$* {
    <fields>;
}

# Material/Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
