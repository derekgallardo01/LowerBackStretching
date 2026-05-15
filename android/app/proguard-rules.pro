# Keep kotlinx.serialization classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.lowerbackstretching.**$$serializer { *; }
-keepclassmembers class com.lowerbackstretching.** {
    *** Companion;
}
-keepclasseswithmembers class com.lowerbackstretching.** {
    kotlinx.serialization.KSerializer serializer(...);
}
