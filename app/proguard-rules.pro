# LicitaFacil
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
-keepattributes Signature, Exception

-keep,includedescriptorclasses class com.licitafacil.app.**$$serializer { *; }
-keepclassmembers class com.licitafacil.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.licitafacil.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-dontwarn okhttp3.**
-dontwarn retrofit2.**
