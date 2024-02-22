# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/njoy/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepattributes AndroidBridge
-keep public class kr.co.lguplus.mucloud.WebViewActivity$AndroidBridge
-keep public class * implements kr.co.lguplus.mucloud.WebViewActivity$AndroidBridge
-keepclassmembers class kr.co.lguplus.mucloud.WebViewActivity$AndroidBridge {
    <methods>;
}

-keepattributes AndroidBridgeBelow17
-keep public class kr.co.lguplus.mucloud.WebViewActivity$AndroidBridgeBelow17
-keep public class * implements kr.co.lguplus.mucloud.WebViewActivity$AndroidBridgeBelow17
-keepclassmembers class kr.co.lguplus.mucloud.WebViewActivity$AndroidBridgeBelow17 {
    <methods>;
}

-keepattributes *Annotation*
-dontwarn com.samsung.android.sdk.***
-dontwarn com.teruten.**