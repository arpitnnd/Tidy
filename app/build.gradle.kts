import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
        create("fossRelease") {
            val storeFileVal = keystoreProperties.getProperty("fossStoreFile")
            val passwordVal = keystoreProperties.getProperty("fossPassword")
            val keyAliasVal = keystoreProperties.getProperty("fossKeyAlias")

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

        create("playHardened") {
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
        // Gates the foss-only "Get Tidy+" row (SettingsMigrationViews.UpgradePromptRow) between
        // its live upgrade action and a "Coming soon" sheet. Only read by the foss flavor; the
        // play flavor drives its own upgrade row off AndroidEntitlementManager.isPlusUnlocked
        // instead. Flip to "true" only once the Tidy+ Play listing is actually live.
        buildConfigField("Boolean", "TIDY_PLUS_AVAILABLE", "false")
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("foss") {
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
            signingConfig = signingConfigs.getByName("fossRelease")
        }
        create("hardened") {
            initWith(getByName("release"))
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            matchingFallbacks.add("release")
            signingConfig = signingConfigs.getByName("playHardened")
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

tasks.register("renameArtifacts") {
    val targetVersionCode = android.defaultConfig.versionCode ?: 1
    val targetBuildDir = layout.buildDirectory.get().asFile

    doLast {
        val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))

        // 1. Rename AABs
        val bundleDir = File(targetBuildDir, "outputs/bundle")
        if (bundleDir.exists()) {
            bundleDir.walkTopDown().forEach { file ->
                if (file.isFile && file.name.endsWith(".aab") && !file.name.startsWith("Tidy-")) {
                    val path = file.absolutePath.lowercase()
                    val flavor =
                        if (path.contains("foss")) "foss" else if (path.contains("play")) "play" else ""
                    val newName = "Tidy-${flavor}-v${targetVersionCode}-${dateStr}.aab"
                    val destFile = File(file.parentFile, newName)
                    if (file.renameTo(destFile)) {
                        println("Renamed bundle: ${file.name} -> $newName")
                    }
                }
            }
        }

        // 2. Rename APKs
        val apkDir = File(targetBuildDir, "outputs/apk")
        if (apkDir.exists()) {
            apkDir.walkTopDown().forEach { file ->
                if (file.isFile && file.name.endsWith(".apk") && !file.name.startsWith("Tidy-")) {
                    val path = file.absolutePath.lowercase()
                    val flavor =
                        if (path.contains("foss")) "foss" else if (path.contains("play")) "play" else ""
                    val newName = "Tidy-${flavor}-v${targetVersionCode}-${dateStr}.apk"
                    val destFile = File(file.parentFile, newName)
                    if (file.renameTo(destFile)) {
                        println("Renamed APK: ${file.name} -> $newName")
                    }
                }
            }
        }
    }
}

tasks.configureEach {
    if ((name.startsWith("assemble") || name.startsWith("bundle")) && !name.contains("Test")) {
        finalizedBy("renameArtifacts")
    }
}
androidComponents {
    beforeVariants { variantBuilder ->
        if (variantBuilder.flavorName == "foss" && variantBuilder.buildType == "hardened") {
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
    testImplementation(libs.mockito.core)

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
