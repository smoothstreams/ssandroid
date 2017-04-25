# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Tim\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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
-keepattributes SourceFile,LineNumberTable

-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn com.squareup.**
-dontwarn okio.**
-dontwarn com.crashlytics.**
-dontwarn com.fasterxml.jackson.databind.**

#-libraryjars libs
-keep class com.crashlytics.** { *; }
-keep class android.support.** { *; }
-keep interface android.support.** { *; }
-keep class com.google.android.** { *; }
-keep class com.applidium.headerlistview.** { *; }

-keep class com.iosharp.android.ssplayer.data.** { *; } 