import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}

android {
    namespace = "com.tidy.app"
    compileSdk = 37

    signingConfigs {
        create("ossRelease") {
            val storeFileVal = keystoreProperties.getProperty("ossStoreFile")
            val passwordVal = keystoreProperties.getProperty("ossPassword")
            val keyAliasVal = keystoreProperties.getProperty("ossKeyAlias")

            if (storeFileVal != null && storeFileVal != "" &&
                passwordVal != null && passwordVal != "" &&
                keyAliasVal != null && keyAliasVal != ""
            ) {
                storeFile = file(storeFileVal)
                storePassword = passwordVal
                keyAlias = keyAliasVal
                keyPassword = passwordVal
            } else {
                val debugSigning = signingConfigs.getByName("debug")
                storeFile = debugSigning.storeFile
                storePassword = debugSigning.storePassword
                keyAlias = debugSigning.keyAlias
                keyPassword = debugSigning.keyPassword
            }
        }

        create("playReleasePlay") {
            val storeFileVal = keystoreProperties.getProperty("playStoreFile")
            val passwordVal = keystoreProperties.getProperty("playPassword")
            val keyAliasVal = keystoreProperties.getProperty("playKeyAlias")

            if (storeFileVal != null && storeFileVal != "" &&
                passwordVal != null && passwordVal != "" &&
                keyAliasVal != null && keyAliasVal != ""
            ) {
                storeFile = file(storeFileVal)
                storePassword = passwordVal
                keyAlias = keyAliasVal
                keyPassword = passwordVal
            } else {
                val debugSigning = signingConfigs.getByName("debug")
                storeFile = debugSigning.storeFile
                storePassword = debugSigning.storePassword
                keyAlias = debugSigning.keyAlias
                keyPassword = debugSigning.keyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "com.tidy.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        buildConfigField("Boolean", "TIDY_PLUS_AVAILABLE", "false")
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("oss") {
            dimension = "distribution"
        }
        create("play") {
            dimension = "distribution"
            applicationIdSuffix = ".play"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("ossRelease")
        }
        create("releasePlay") {
            initWith(getByName("release"))
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            matchingFallbacks.add("release")
            signingConfig = signingConfigs.getByName("playReleasePlay")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        aidl = false
        buildConfig = true
        shaders = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

androidComponents {
    beforeVariants { variantBuilder ->
        if (variantBuilder.flavorName == "oss" && variantBuilder.buildType == "releasePlay") {
            variantBuilder.enable = false
        }
        if (variantBuilder.flavorName == "play" && variantBuilder.buildType == "release") {
            variantBuilder.enable = false
        }
    }
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Arch Components
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)

    // Tooling
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Instrumented tests
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Local tests: jUnit, coroutines, Android runner
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // Instrumented tests: jUnit rules and runners
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)

    // Navigation
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)

    // ACRA local-only crash reporting
    implementation(libs.acra.core)
    implementation(libs.acra.dialog)

    // Dynamic feature-plus dependency (for play build flavor only)
    if (project.findProject(":feature-plus") != null) {
        add("playImplementation", project(":feature-plus"))
    }

    // Shared Multiplatform Module
    implementation(project(":shared"))
}
