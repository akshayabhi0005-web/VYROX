# Proguard rules for VYROX Native Android App (Team VELTRION)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Models for Gson serialization
-keep class com.veltrion.vyrox.data.model.** { *; }

# Retrofit & OkHttp
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
