# R8 configuration for release builds.
#
# Shrinking, obfuscation and resource shrinking are all enabled (see
# app/build.gradle.kts). Bundled puzzle drawables are resolved reflectively via
# Resources.getIdentifier(), so they are pinned for the resource shrinker in
# res/raw/keep.xml.
#
# Most libraries here (Hilt, Room, Compose, kotlinx-coroutines) ship their own
# consumer rules via AAR metadata, so this file covers only what those do not.

# Keep line numbers and map them back through the mapping file, so Play Console
# crash reports stay readable after obfuscation.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Runtime annotations and generic signatures are needed by Room's and Hilt's
# generated code and by Kotlin reflection on suspend/serialized types.
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepattributes Signature,InnerClasses,EnclosingMethod,AnnotationDefault

# Domain models are persisted to Room / DataStore and restored by name.
# Keeping members prevents field renaming from breaking stored rows.
-keep class com.tessera.puzzle.domain.model.** { *; }

# Kotlin intrinsics emit these on null checks; stripping the messages is safe
# and removes parameter-name strings from the release binary.
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNullParameter(java.lang.Object, java.lang.String);
    public static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
}

# Strip verbose/debug logging from release builds.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
