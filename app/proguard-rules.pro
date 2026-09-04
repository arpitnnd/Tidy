# Project-specific ProGuard / R8 rules.
# Appended to the base flags from getDefaultProguardFile("proguard-android-optimize.txt").

# Keep annotations
-keepattributes *Annotation*,InnerClasses

# Preserve classes and members annotated with @Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Preserve kotlinx.serialization classes, serialized properties, and generated serializers
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName *;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * extends kotlinx.serialization.internal.GeneratedSerializer {
    *;
}
