# 01 — Tech Stack & Build

```
GRADLE (Kotlin DSL)
├── android.application          (single :app module)
├── kotlin.compose               (Compose compiler + runtime)
├── kotlin.serialization         (JSON for UserDocument)
└── repos: google(), mavenCentral()

DEPENDENCIES (app/build.gradle.kts)
├── androidx.compose.bom + material3 + foundation + ui + windowsizeclass
├── androidx.activity.compose
├── androidx.lifecycle.viewmodel.compose / runtime-ktx
├── androidx.work.runtime.ktx                 (WorkManager)
├── androidx.security.crypto                  (EncryptedSharedPreferences candidate)
├── com.google.android.gms:play-services-nearby (P2P Nearby)
├── org.jetbrains.kotlinx:kotlinx-serialization-json
├── com.google.zxing:core                     (barcode/QR core)
└── tests: junit, espresso, compose ui.test

APP METADATA
  namespace   = com.example.inscit
  minSdk      = 24
  targetSdk   = 36
  compileSdk  = 36
  Java        = 17
  versionCode = 2
  versionName = 1.1 (UI displays v9.0.4)
  ProGuard    = enabled on release
```
