# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/tautvydas/android-sdk-linux/tools/proguard/proguard-android.txt
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

# Workaround for Samsung Android 4.2 bug
# https://code.google.com/p/android/issues/detail?id=78377
# https://code.google.com/p/android/issues/detail?id=78377#c188
-keepattributes **
-keep class !android.support.v7.internal.view.menu.**,** {*;}
-dontpreverify
-dontoptimize
-dontshrink
-dontwarn **
-dontnote **