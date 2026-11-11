# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# kotlinx.serialization uses reflection to resolve @Serializable
# serializers. Keep the runtime and generated serializer metadata so
# Json.encodeToString/decodeFromString keep working under minification.
-keep class kotlinx.serialization.** { *; }
-keepnames class kotlinx.serialization.internal.** { *; }

# App data models are @Serializable; keep them and their generated
# serializers so JSON encode/decode is not broken by renaming.
-keep class com.example.inscit.models.** { *; }