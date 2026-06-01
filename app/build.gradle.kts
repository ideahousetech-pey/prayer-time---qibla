import java.net.URL
import java.io.File

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "id.ideahousetech.prayertime_qibla"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "id.ideahousetech.prayertime_qibla"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true          // ← WAJIB aktifkan
      isShrinkResources = true        // ← tambahkan ini
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.text.googlefonts)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
  implementation(libs.androidx.security.crypto)
}

abstract class DownloadAssetsTask : DefaultTask() {
    @get:OutputDirectory
    abstract val assetsDir: DirectoryProperty

    @get:OutputDirectory
    abstract val fontDir: DirectoryProperty

    @TaskAction
    fun run() {
        val targetAssets = assetsDir.get().asFile
        if (!targetAssets.exists()) {
            targetAssets.mkdirs()
        }
        val adzanFile = File(targetAssets, "adzan.mp3")
        if (!adzanFile.exists() || adzanFile.length() < 10) {
            try {
                println("Downloading adzan.mp3...")
                val connection = URL("https://archive.org/download/adhan_202206/adhan.mp3").openConnection()
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.getInputStream().use { input ->
                    adzanFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                println("adzan.mp3 download success.")
            } catch (e: Exception) {
                println("Failed to download adzan.mp3: ${e.message}")
                adzanFile.writeBytes(ByteArray(1))
            }
        }
        val adzanFajrFile = File(targetAssets, "adzan_fajr.mp3")
        if (!adzanFajrFile.exists() || adzanFajrFile.length() < 10) {
            try {
                println("Downloading adzan_fajr.mp3...")
                val connection = URL("https://archive.org/download/AzanMadinah_201712/azan_madinah.mp3").openConnection()
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.getInputStream().use { input ->
                    adzanFajrFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                println("adzan_fajr.mp3 download success.")
            } catch (e: Exception) {
                println("Failed to download adzan_fajr.mp3: ${e.message}")
                adzanFajrFile.writeBytes(ByteArray(1))
            }
        }
        
        // Fonts are now handled via robust native System Font Families (Serif and SansSerif)
        // to prevent any 503 network-related startup crashes on corrupt font files.
    }
}

val downloadAssets = tasks.register<DownloadAssetsTask>("downloadAssets") {
    assetsDir.set(layout.projectDirectory.dir("src/main/assets"))
    fontDir.set(layout.projectDirectory.dir("src/main/res/font"))
}

tasks.named("preBuild") {
    dependsOn(downloadAssets)
}





