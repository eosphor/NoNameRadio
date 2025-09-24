# Keep our Application and prevent member obfuscation for safer reflection
-keep class com.nonameradio.app.NoNameRadioApp { *; }

# MikePenz Android-Iconics: keep typefaces and avoid method stripping
-keep class com.mikepenz.iconics.** { *; }
-keep class com.mikepenz.iconics.typeface.** { *; }
-keep class com.mikepenz.iconics.typeface.library.** { *; }
-keep class com.mikepenz.iconics.typeface.library.googlematerial.** { *; }

# Glide model loader classes used via reflection
-keep class com.bumptech.glide.integration.okhttp3.** { *; }
-keep class com.bumptech.glide.load.model.GlideUrl { *; }
-keep class com.nonameradio.app.utils.ImageLoader { *; }

# OkHttp/Okio names (usually safe, but avoid over-shrinking)
-dontwarn okhttp3.**
-dontwarn okio.**

# Yandex Metrica SDK
-keep class com.yandex.metrica.** { *; }
-keep class com.yandex.metrica.impl.** { *; }
-dontwarn com.yandex.metrica.**

# Country dictionaries and flags (used via singletons, avoid obfuscation issues)
-keep class com.nonameradio.app.CountryCodeDictionary { *; }
-keep class com.nonameradio.app.CountryFlagsLoader { *; }

# Keep enums and annotations intact
-keepclassmembers enum * { *; }
-keepattributes *Annotation*

# Keep any classes referenced from AndroidManifest (activities, services, receivers)
-keep class ** extends android.app.Activity { *; }
-keep class ** extends android.app.Service { *; }
-keep class ** extends android.content.BroadcastReceiver { *; }
-keep class ** extends android.content.ContentProvider { *; }

# Room schemas (if needed by reflection)
-keep class androidx.room.** { *; }
-keep class com.nonameradio.app.database.** { *; }

# Prevent stripping of EventBus listeners (our custom bus uses generics lambdas; safe)
-keep class com.nonameradio.app.core.event.** { *; }
