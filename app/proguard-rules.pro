# Keep Android for Cars App library classes
-keep class androidx.car.app.** { *; }
-keep interface androidx.car.app.** { *; }
-keep class com.carbrowser.auto.** { *; }

# Keep WebView JavaScript interfaces
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
