import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.dokka")
}

android {
    namespace = "es.didaktikapp.gernikapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "es.didaktikapp.gernikapp"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // URL de la API - Se lee desde local.properties
        val localProperties = File(rootProject.projectDir, "local.properties")
        val properties = Properties()
        if (localProperties.exists()) {
            properties.load(FileInputStream(localProperties))
        }

        val apiUrl = properties.getProperty("API_BASE_URL") ?: "http://10.0.2.2:8000"
        buildConfigField("String", "API_BASE_URL", "\"$apiUrl\"")

        // Cloudinary configuration
        val cloudinaryCloudName = properties.getProperty("CLOUDINARY_CLOUD_NAME") ?: ""
        val cloudinaryApiKey = properties.getProperty("CLOUDINARY_API_KEY") ?: ""
        val cloudinaryApiSecret = properties.getProperty("CLOUDINARY_API_SECRET") ?: ""
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudinaryCloudName\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"$cloudinaryApiKey\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"$cloudinaryApiSecret\"")
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
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.flexbox)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)

    // Networking - Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // JSON - Moshi
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // Security - Encrypted SharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Cloudinary - Image upload
    implementation("com.cloudinary:cloudinary-android:3.0.2")

    // Coil - Image loading from URL
    implementation("io.coil-kt:coil:2.5.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}