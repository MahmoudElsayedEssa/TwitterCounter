import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Read OAuth credentials from local.properties
fun String.escapeForBuildConfig(): String = replace("\"", "\\\"")

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

val twitterClientId: String =
    (localProperties.getProperty("TWITTER_CLIENT_ID") ?: "").escapeForBuildConfig()
val twitterRedirectUri: String =
    (localProperties.getProperty("TWITTER_REDIRECT_URI") ?: "").escapeForBuildConfig()
val twitterBearerToken: String =
    (localProperties.getProperty("TWITTER_BEARER_TOKEN") ?: "").escapeForBuildConfig()

android {
    namespace = "com.moe.twitter"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.moe.twitter"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Twitter API Configuration
        buildConfigField("String", "TWITTER_BASE_URL", "\"https://api.twitter.com/2/\"")
        buildConfigField("String", "TWITTER_CLIENT_ID", "\"$twitterClientId\"")
        buildConfigField("String", "TWITTER_REDIRECT_URI", "\"$twitterRedirectUri\"")
        buildConfigField("String", "TWITTER_BEARER_TOKEN", "\"$twitterBearerToken\"")

        // LanguageTool API
        buildConfigField("String", "LANGUAGE_TOOL_BASE_URL", "\"https://api.languagetool.org/v2/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Unit Testing
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("io.insert-koin:koin-test:4.0.0")
    testImplementation("io.insert-koin:koin-test-junit4:4.0.0")

    // Android Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    // Twitter
    implementation(libs.twittertext.twitter.text)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Lottie
    implementation(libs.lottie.compose)

    // Security (for EncryptedSharedPreferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
