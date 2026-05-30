# ─── Obfuscation Agresif ───────────────────────────────────────
-repackageclasses 'o'
-allowaccessmodification
-optimizationpasses 5
-dontusemixedcaseclassnames

# ─── Hapus semua Log di Release Build ─────────────────────────
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# ─── Jaga Kotlin & Coroutines ─────────────────────────────────
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-keepclassmembers class kotlin.Metadata { *; }
-keep class kotlin.** { *; }

# ─── Jaga Moshi (JSON serialization) ──────────────────────────
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class com.squareup.moshi.** { *; }

# ─── Jaga Retrofit & OkHttp ───────────────────────────────────
-keep interface * { @retrofit2.http.* <methods>; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# ─── Jaga AndroidManifest components ──────────────────────────
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Application

# ─── Jaga ViewModel & Compose ─────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keep class androidx.compose.** { *; }

# ─── Jaga Room Database ───────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# ─── Jaga model data app ──────────────────────────────────────
-keep class id.ideahousetech.prayertime_qibla.model.** { *; }

# ─── Jaga Play Services Location ──────────────────────────────
-keep class com.google.android.gms.location.** { *; }

# ─── Sembunyikan nama file source ─────────────────────────────
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
