# Jetpack Compose-specific ProGuard rules
-keep class androidx.compose.ui.platform.AndroidComposeView { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable class *;
    @androidx.compose.runtime.ReadOnlyComposable class *;
}
-keep class androidx.compose.animation.core.InfiniteTransition { *; }

# Room Database ProGuard Rules
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.data.database.dao.** { *; }
-keep class com.example.data.database.entity.** { *; }
-keep class com.example.data.database.converters.** { *; }
-keep class * implements com.example.data.database.dao.**
-dontwarn androidx.room.paging.**

# Keep our domain models and DTOs (important for serialization/Room mapping)
-keep class com.example.domain.model.** { *; }
-keep class com.example.presentation.screens.** { *; }
-keep class com.example.data.model.** { *; }

# Hilt & Dagger keep rules
-keep class * extends android.app.Application
-keep interface dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep class *__HiltComponents* { *; }
-keep @dagger.hilt.EntryPoints class * { *; }

# Jetpack Navigation keep rules
-keep class * extends androidx.navigation.Navigator { *; }
-keep class androidx.navigation.compose.** { *; }
-keepclassmembers class * extends androidx.navigation.NavArgs { *; }

# Storage Access Framework & FileProvider keep rules
-keep class androidx.core.content.FileProvider { *; }
-keepclassmembers class * extends androidx.core.content.FileProvider { *; }

# Jetpack DataStore keep rules
-keep class androidx.datastore.preferences.protobuf.** { *; }
-keep class androidx.datastore.preferences.core.** { *; }
-dontwarn androidx.datastore.**

# Google Mobile Ads SDK (AdMob) Rules
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep class com.google.android.gms.common.api.internal.IAppOpsCallback* { *; }
-keep class com.google.android.gms.common.api.internal.IAppOpsService* { *; }
-dontwarn com.google.android.gms.ads.**

# Keep Notification Listener Service
-keep class com.example.core.notification.NotificationListenerService { *; }
-keep class com.example.core.notification.NotificationLogger { *; }
-keepclassmembers class com.example.core.notification.NotificationListenerService {
    public <methods>;
}

# Keep AppContainer and dependency injection components
-keep class com.example.core.di.AppContainer { *; }
-keep class com.example.core.log.InMemoryLogStore { *; }
-keep class com.example.core.log.Logger { *; }
-keep class com.example.core.log.AndroidLogger { *; }

# Kotlin Coroutines & Flow
-keep class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.flow.** { *; }

# Keep ViewModels and State classes
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Moshi JSON library Proguard rules
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json class *;
    @com.squareup.moshi.JsonQualifier class *;
}
-keep class * implements com.squareup.moshi.JsonAdapter { *; }
-keep class * extends com.squareup.moshi.JsonAdapter { *; }
-dontwarn com.squareup.moshi.**

# Preserve line numbers and source file names for production crash logs
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

