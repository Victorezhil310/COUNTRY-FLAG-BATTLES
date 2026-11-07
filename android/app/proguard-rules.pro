# Full mode R8 Optimization & Class Repackaging
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''
-overloadaggressively
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,*Annotation*

# Preserve JavaScript interface methods for WebView Bridges
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Preserve MainActivity and inner classes
-keep class com.victorflagsa.boostflagrush.MainActivity** { *; }
-keepclassmembers class com.victorflagsa.boostflagrush.MainActivity$TtsBridge { *; }
-keepclassmembers class com.victorflagsa.boostflagrush.MainActivity$PlayGamesBridge { *; }
-keepclassmembers class com.victorflagsa.boostflagrush.MainActivity$AdMobBridge { *; }
-keepclassmembers class com.victorflagsa.boostflagrush.MainActivity$IntegrityBridge { *; }
-keepclassmembers class com.victorflagsa.boostflagrush.MainActivity$FeedbackBridge { *; }

# Preserve Google Play Services Ads, Games, & Integrity API classes
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep class com.google.android.gms.games.** { *; }
-keep class com.google.android.play.core.integrity.** { *; }
