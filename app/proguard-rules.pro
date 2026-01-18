# remove Log.* lines
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
# ======================================================================================
#----------------------------------------------------------------------------------
# This is a rule that might have been automatically added by the build tools.
# It's safe to keep. It suppresses warnings about a hidden system class.
-dontwarn android.media.LoudnessCodecController
-dontwarn android.media.LoudnessCodecController$*

#----------------------------------------------------------------------------------
# Android Jetpack and Default Rules
# These rules prevent common crashes with standard Android components.
#
-keepattributes *Annotation*
-keep class * extends android.app.Activity
-keep class * extends android.app.Application
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

#----------------------------------------------------------------------------------
# Google Mobile Ads SDK (AdMob)
# Absolutely required for ads to work in release builds.
# Prevents the SDK from being broken by code shrinking.
#
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}
-dontwarn com.google.android.gms.ads.**

#----------------------------------------------------------------------------------
# Firebase Analytics
# Required for Firebase Analytics to correctly log events and user properties
# in release builds.
#
-keep class com.google.android.gms.measurement.AppMeasurement$ConditionalUserProperty { *; }
-keep class com.google.firebase.analytics.FirebaseAnalytics$Param { *; }
-keep class com.google.firebase.analytics.FirebaseAnalytics$Event { *; }
# ... (the firebase analytics rules above it) ...
-keepnames @com.google.android.gms.common.annotation.KeepName class *

# Suppress the "unresolved class" warning for the line below. It is a known false positive.
#noinspection ShrinkerUnresolvedReference
-keepnames class * extends com.google.android.gms.internal.firebase_perf.zzb

#----------------------------------------------------------------------------------
# JLayer MP3 Library (jl1.0.1.jar)
# ... (the rest of the file) ...



#----------------------------------------------------------------------------------
# JLayer MP3 Library (jl1.0.1.jar)
# Since this is an older Java library, it's safest to prevent ProGuard from
# renaming or removing any of its classes, especially the decoders and converters.
#
-keep class javazoom.jl.** { *; }
-dontwarn javazoom.jl.**
# ======================= THE FINAL, OBVIOUS FIX =======================
#
# YOUR DIAGNOSIS WAS PERFECT. THIS IS THE FIX.
# These rules tell ProGuard/R8 not to obfuscate or remove critical
# classes and methods used by Firebase and Google Play Services.
# This will allow Firebase Analytics to initialize correctly in your
# 'release' build.
#

# General rule for keeping data classes used by Firebase
-keepattributes Signature
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.android.gms.measurement.** { *; }

# Keep required for Google Play Services measurement and app measurement
-keep public class com.google.android.gms.measurement.AppMeasurement {
    # ======================= THE FINAL, SYNTAX FIX =======================
    #
    # YOUR DIAGNOSIS WAS PERFECT. THIS IS THE REAL FIX.
    # The ProGuard parser is choking on the generic <T> syntax.
    # We simplify the signature to its non-generic form.
    #
    public static java.lang.Object getInstance(android.content.Context);
    #
    # =======================================================================

    public void logEvent(java.lang.String, android.os.Bundle);
}

