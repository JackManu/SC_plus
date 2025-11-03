-dontwarn android.media.LoudnessCodecController
-dontwarn android.media.LoudnessCodecController$*
proguard
#----------------------------------------------------------------------------------
# Android Jetpack and Default Rules
#
# These lines are typically at the top of a new ProGuard file. They include rules for
# common Android features and ensure that default constructors and certain annotations
# are kept, which helps prevent common crashes with Activities and Services.
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
#
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
#
# Required for Firebase Analytics to correctly log events and user properties
# in release builds.
#
-keep class com.google.android.gms.measurement.AppMeasurement$ConditionalUserProperty { *; }
-keep class com.google.firebase.analytics.FirebaseAnalytics$Param { *; }
-keep class com.google.firebase.analytics.FirebaseAnalytics$Event { *; }
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepnames class * extends com.google.android.gms.internal.firebase-perf.zzb

#----------------------------------------------------------------------------------
# JLayer MP3 Library (jl1.0.1.jar)
#
# Since this is an older Java library, it's safest to prevent ProGuard from
# renaming or removing any of its classes, especially the decoders and converters
# which are likely accessed dynamically.
#
-keep class javazoom.jl.** { *; }
-dontwarn javazoom.jl.**


   public *;
  }
-keep public class com.google.ads.** {
   public *;
  }
-keepattributes *Annotation*
-dontwarn com.google.android.gms.ads.**
        
