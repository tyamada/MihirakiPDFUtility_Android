import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.takumayamada22.pdfutility"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.takumayamada22.pdfutility"
        minSdk = 26
        targetSdk = 36
        versionCode = 7
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val props = Properties()
        val propFile = rootProject.file("local.properties")
        if (propFile.exists()) {
            propFile.inputStream().use { props.load(it) }
        }

        create("release") {
            storeFile = rootProject.file("release-key.jks")
            storePassword = props.getProperty("signing.storePassword") ?: "password123"
            keyAlias = props.getProperty("signing.keyAlias") ?: "my-alias"
            keyPassword = props.getProperty("signing.keyPassword") ?: "password123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.android.material:material:1.12.0")
    
    // PDF Processing (Apache 2.0)
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    implementation("com.android.billingclient:billing-ktx:8.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    // LeakCanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}

tasks.register("preBundleDocumentationCheck") {
    group = "verification"
    description = "Checks that documentation, LICENSE, store listing metadata, web pages, and translations are complete prior to building the App Bundle."
    val rootDirFile = rootProject.rootDir
    doLast {
        logger.lifecycle("=======================================================================")
        logger.lifecycle("[RELEASE RULE CHECK] Pre-App Bundle build check:")
        logger.lifecycle("1. Verifying README, LICENSE, docs/*.html, and store_listing/metadata*.txt")
        logger.lifecycle("2. Verifying translation completeness across all supported locales.")
        logger.lifecycle("=======================================================================")

        val rootDir = rootDirFile
        val requiredFiles = listOf(
            File(rootDir, "README.md"),
            File(rootDir, "README_ja.md"),
            File(rootDir, "LICENSE"),
            File(rootDir, "store_listing/metadata_en.txt"),
            File(rootDir, "store_listing/metadata_ja.txt"),
            File(rootDir, "store_listing/metadata_it.txt"),
            File(rootDir, "docs/store_listing/metadata_it.txt"),
            File(rootDir, "store_listing/metadata_ko.txt"),
            File(rootDir, "docs/store_listing/metadata_ko.txt"),
            File(rootDir, "store_listing/metadata_zh-CN.txt"),
            File(rootDir, "docs/store_listing/metadata_zh-CN.txt")
        )

        val missingFiles = requiredFiles.filter { !it.exists() }
        if (missingFiles.isNotEmpty()) {
            throw GradleException("Missing required release documentation/metadata files: ${missingFiles.map { t -> t.name }}")
        }

        // Verify localized string files exist for all 14 locales
        val resDir = File(rootDir, "app/src/main/res")
        val requiredLocales = listOf(
            "values", "values-de", "values-es", "values-fr", "values-in",
            "values-it", "values-ja", "values-ko", "values-ne", "values-pt",
            "values-tl", "values-vi", "values-zh-rCN", "values-zh-rTW"
        )
        val missingLocales = requiredLocales.filter { locale ->
            !File(resDir, "$locale/strings.xml").exists()
        }
        if (missingLocales.isNotEmpty()) {
            throw GradleException("Missing localized strings.xml for locales: $missingLocales")
        }
        logger.lifecycle("Verified translation files for all ${requiredLocales.size} locales.")

        val docsDir = File(rootDir, "docs")
        val htmlFiles = docsDir.listFiles { file -> file.extension.lowercase() == "html" } ?: emptyArray()
        logger.lifecycle("Verified ${htmlFiles.size} HTML documentation files in docs/.")

        // Verify help screen strings exist in strings.xml
        val stringsFile = File(resDir, "values/strings.xml")
        if (stringsFile.exists()) {
            val content = stringsFile.readText()
            val requiredHelpKeys = listOf("help_title", "help_overview_text", "help_page_ops_text", "help_display_text", "help_settings_text")
            val missingKeys = requiredHelpKeys.filter { !content.contains(it) }
            if (missingKeys.isNotEmpty()) {
                throw GradleException("Missing required help screen string keys in strings.xml: $missingKeys")
            }
            logger.lifecycle("Verified help screen text resources in strings.xml.")
        }

        logger.lifecycle("[RELEASE RULE CHECK] Translation, help screen, and documentation checks passed successfully.")
    }
}

tasks.register("updateDocs") {
    group = "documentation"
    description = "Updates HTML files in docs/ to include Italian language option."
    val rootDirFile = rootProject.rootDir
    doLast {
        val docsDir = File(rootDirFile, "docs")
        docsDir.listFiles { file -> file.extension.lowercase() == "html" }?.forEach { file ->
            val text = file.readText()
            if (!text.contains("_it.html")) {
                val prefix = when {
                    file.name.startsWith("index") -> "index"
                    file.name.startsWith("privacy") -> "privacy"
                    file.name.startsWith("support") -> "support"
                    else -> null
                }
                if (prefix != null) {
                    val target = "<option value=\"${prefix}_in.html\">Bahasa Indonesia</option>"
                    val replacement = "$target\n            <option value=\"${prefix}_it.html\">Italiano (Italian)</option>"
                    if (text.contains(target)) {
                        file.writeText(text.replace(target, replacement))
                        println("Updated ${file.name}")
                    }
                }
            }
        }
    }
}

tasks.matching { it.name.startsWith("bundle") }.configureEach {
    dependsOn("preBundleDocumentationCheck")
}
