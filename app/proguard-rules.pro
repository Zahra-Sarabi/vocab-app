# kotlinx.serialization: keep generated serializers for content, backups and navigation routes.
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.vocab.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.vocab.**$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class com.vocab.** {
    *** Companion;
}
