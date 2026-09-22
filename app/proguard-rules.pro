# PDFBox-Android rules
-keep class com.tom_roush.pdfbox.** { *; }
-keep class org.apache.fontbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**
-dontwarn org.apache.fontbox.**

# Material/Compose rules (usually handled by AARs but good to have basics)
-keep class androidx.compose.** { *; }
