-dontwarn org.slf4j.impl.StaticLoggerBinder
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
#-keep class io.kamel.** { *; }

-dontwarn net.jcip.annotations.Immutable
-dontwarn net.jcip.annotations.ThreadSafe
-dontwarn com.google.crypto.tink.subtle.XChaCha20Poly1305