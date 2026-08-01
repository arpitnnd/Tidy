plugins {
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

// Generates a Kotlin constant from blocklist/trackers.v2.json (the repo's single authored
// tracker-param list) so the compiled-in default is never hand-copied out of sync with it.
//
// blocklist/trackers.json (no "v2") is a *different*, frozen file: it's the path
// BlocklistSyncer's 1.1.0 release already shipped pointing at, using a schema without the
// "domains" field. Editing it further would silently break remote blocklist syncing for
// every 1.1.0 install the moment it reached main, since their strict (non-
// ignoreUnknownKeys) JSON decoder throws on the unrecognised field and the sync silently
// stops retrying-and-failing forever. New schema versions get their own versioned
// filename instead; this task and BlocklistSyncer's fetch URL both point at the latest one
// for the version currently being built.
val generateTrackerDefaults = tasks.register("generateTrackerDefaults") {
    val blocklistFile = rootProject.file("blocklist/trackers.v2.json")
    val outputDir = layout.buildDirectory.dir("generated/tidy/trackers/commonMain/kotlin")
    inputs.file(blocklistFile)
    outputs.dir(outputDir)
    doLast {
        val json = blocklistFile.readText().trim()
        require(!json.contains("\"\"\"")) {
            "blocklist/trackers.v2.json contains a triple-quote sequence; cannot embed in a Kotlin raw string."
        }
        require(!json.contains('$')) {
            "blocklist/trackers.v2.json contains '$'; cannot embed in a const val raw string."
        }
        val outFile = outputDir.get().asFile.resolve("com/tidy/app/data/GeneratedBlocklist.kt")
        outFile.parentFile.mkdirs()
        outFile.writeText(
            "package com.tidy.app.data\n\n" +
                    "// GENERATED from blocklist/trackers.v2.json by :shared:generateTrackerDefaults. Do not edit by hand.\n" +
                    "internal const val GENERATED_DEFAULT_BLOCKLIST_JSON: String = \"\"\"$json\"\"\"\n"
        )
    }
}

kotlin {
    android {
        namespace = "com.tidy.app.shared"
        compileSdk = 37
        minSdk = 24

        withHostTestBuilder {
            sourceSetTreeName = "test"
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generateTrackerDefaults.map { it.outputs.files.singleFile })
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.androidx.datastore)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.junit)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
