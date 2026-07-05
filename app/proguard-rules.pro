# Jetpack Compose-specific ProGuard rules
-keep class androidx.compose.ui.platform.AndroidComposeView { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable class *;
    @androidx.compose.runtime.ReadOnlyComposable class *;
}

# Room Database ProGuard Rules
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.data.database.dao.** { *; }
-keep class com.example.data.database.entity.** { *; }
-keep class com.example.data.database.converters.** { *; }
-keep class * implements com.example.data.database.dao.**

# Keep our domain models and DTOs (important for serialization/Room mapping)
-keep class com.example.domain.model.** { *; }
-keep class com.example.presentation.screens.** { *; }
-keep class com.example.data.model.** { *; }

# Google Mobile Ads SDK (AdMob) Rules
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep class com.google.android.gms.common.api.internal.IAppOpsCallback* { *; }
-keep class com.google.android.gms.common.api.internal.IAppOpsService* { *; }

# Keep Notification Listener Service
-keep class com.example.core.notification.NotificationListenerService { *; }
-keep class com.example.core.notification.NotificationLogger { *; }

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

# Preserve line numbers and source file names for production crash logs
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
