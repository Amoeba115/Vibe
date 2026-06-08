plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "me.ayra.music"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "me.ayra.music"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "IS_VGM_BUILD", "false")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "IS_VGM_BUILD", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("debugVGM") {
            initWith(getByName("debug"))
            matchingFallbacks += listOf("debug")
            //applicationIdSuffix = ".vgm.debug"
            versionNameSuffix = "-vgm-debug"
            isDebuggable = true
            buildConfigField("boolean", "IS_VGM_BUILD", "true")
        }
        create("releaseVGM") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            //applicationIdSuffix = ".vgm"
            versionNameSuffix = "-vgm"
            buildConfigField("boolean", "IS_VGM_BUILD", "true")
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.coil.compose)
    implementation(libs.google.material)
    implementation(libs.androidx.palette.ktx)
    add("debugVGMImplementation", project(":vgmstream-core"))
    add("debugVGMImplementation", project(":vgmstream-media3"))
    add("releaseVGMImplementation", project(":vgmstream-core"))
    add("releaseVGMImplementation", project(":vgmstream-media3"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
