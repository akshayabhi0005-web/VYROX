plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.veltrion.vyrox"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.veltrion.vyrox"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val facebookAppId = System.getenv("FACEBOOK_APP_ID") ?: "vyrox_fb_app"
        val facebookClientToken = System.getenv("FACEBOOK_CLIENT_TOKEN") ?: "vyrox_fb_token"

        manifestPlaceholders["FACEBOOK_APP_ID"] = facebookAppId
        manifestPlaceholders["FACEBOOK_CLIENT_TOKEN"] = facebookClientToken
    }

    signingConfigs {
        create("release") {
            // Configurable from environment/properties
            storeFile = file("release-keystore.jks")
            storePassword = System.getenv("VYROX_KEYSTORE_PASSWORD") ?: "vyroxReleaseKey2026"
            keyAlias = System.getenv("VYROX_KEY_ALIAS") ?: "vyrox_key"
            keyPassword = System.getenv("VYROX_KEY_PASSWORD") ?: "vyroxReleaseKey2026"
        }
    }

    buildTypes {
        release {
            val releaseApiUrl = System.getenv("RELEASE_API_BASE_URL") ?: "https://api.vyrox.com/api/v1/"
            buildConfigField("String", "API_BASE_URL", "\"$releaseApiUrl\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            val debugApiUrl = System.getenv("DEBUG_API_BASE_URL") ?: "http://10.0.2.2:8080/api/v1/"
            buildConfigField("String", "API_BASE_URL", "\"$debugApiUrl\"")
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    // Location & Google Identity (Google Sign-In)
    implementation(libs.play.services.location)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Networking & Serialization
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Image loading
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.ui.tooling)
}
