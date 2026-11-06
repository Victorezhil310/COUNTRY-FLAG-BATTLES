# Preserve JavaScript interface methods for WebView TTS Bridge
-keepattributes *Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Preserve MainActivity and inner classes
-keep class com.victorflagsa.boostflagrush.MainActivity** { *; }
-keepclassmembers class com.victorflagsa.boostflagrush.MainActivity$TtsBridge { *; }
