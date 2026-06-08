# Add project specific ProGuard rules here.

# Keep kotlinx.serialization metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mauricior8.enfoque.**$$serializer { *; }
-keepclassmembers class com.mauricior8.enfoque.** {
    *** Companion;
}
-keepclasseswithmembers class com.mauricior8.enfoque.** {
    kotlinx.serialization.KSerializer serializer(...);
}
