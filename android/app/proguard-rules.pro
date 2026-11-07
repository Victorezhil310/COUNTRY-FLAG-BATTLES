# Preserve JavaScript interface methods for WebView Bridges
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Preserve MainActivity and inner classes
-keep class com.victorflagsa.boostflagrush.MainActivity** { *; }
-keepclassmembers class com.victorflagsa.boostflagrush.MainActivity$TtsBridge { *; }
-keepclassmembers class com.victorflagsa.boostflagrush.MainActivity$PlayGamesBridge { *; }
-keepclassmembers class com.victorflagsa.boostflagrush.MainActivity$AdMobBridge { *; }

# Preserve Google Play Services Ads classes
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep class com.google.android.gms.games.** { *; }
