# Optimization and Obfuscation Rules for OmniTools Release

# Keep Main Entry points and Activity
-keep public class com.example.MainActivity { *; }
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep AdMob / Google Mobile Ads classes
-keep class com.google.android.gms.ads.** { *; }
-keep public class com.example.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Keep ZXing QR/Barcode classes
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# Reflection-sensitive Compose bridge class
-keep class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt {
    public static androidx.compose.runtime.ProvidableCompositionLocal getLocalLifecycleOwner();
}
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
