import java.io.FileInputStream
import java.util.Properties
import java.time.LocalDateTime

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
    id("com.google.dagger.hilt.android") version "2.48"
    kotlin("kapt")
}

// Load keystore properties dari file terpisah (tidak di-commit ke Git)
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.bdajaya.adminku"
    // PERBAIKAN: Update ke versi Android terbaru
    compileSdk = 36
    // PERBAIKAN: Tambah ini untuk target SDK terbaru
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "com.bdajaya.adminku"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // ProGuard configuration files
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )

        // Hanya sertakan resource untuk bahasa Indonesia dan Inggris
        @Incubating
        androidResources.localeFilters += setOf("id", "en")

        buildConfigField("boolean", "IS_DEBUG", "false")
        buildConfigField("String", "BUILD_TIME", "\"${System.currentTimeMillis()}\"")
    }

    // Room annotation processor configuration untuk KSP
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }

    signingConfigs {
        // Release signing configuration
        val release by creating {
            val storeFilePath = keystoreProperties.getProperty("storeFile")
            if (storeFilePath != null) {
                val keystoreFile = file(storeFilePath)
                if (keystoreFile.exists()) {
                    storeFile = keystoreFile
                    storePassword = keystoreProperties.getProperty("storePassword") ?: ""
                    keyAlias = keystoreProperties.getProperty("keyAlias") ?: ""
                    keyPassword = keystoreProperties.getProperty("keyPassword") ?: ""

                    enableV1Signing = true
                    enableV2Signing = true
                    enableV3Signing = true
                    enableV4Signing = true

                    storeFile?.let { file ->
                        println("Using release keystore: ${file.absolutePath}")
                    }
                } else {
                    println("WARNING: Release keystore file not found at: $storeFilePath")
                    setupDebugKeystore(this)
                }
            } else {
                println("WARNING: Release keystore path not configured.")
                setupDebugKeystore(this)
            }
        }

        // Debug signing configuration - GUNAKAN RELEASE KEYSTORE
        getByName("debug") {
            val storeFilePath = keystoreProperties.getProperty("storeFile")
            if (storeFilePath != null) {
                val keystoreFile = file(storeFilePath)
                if (keystoreFile.exists()) {
                    storeFile = keystoreFile
                    storePassword = keystoreProperties.getProperty("storePassword") ?: ""
                    keyAlias = keystoreProperties.getProperty("keyAlias") ?: ""
                    keyPassword = keystoreProperties.getProperty("keyPassword") ?: ""

                    enableV1Signing = true
                    enableV2Signing = true
                    enableV3Signing = true
                    enableV4Signing = true

                    storeFile?.let { file ->
                        println("Using release keystore for debug builds: ${file.absolutePath}")
                    }
                } else {
                    println("WARNING: Release keystore file not found at: $storeFilePath")
                    setupDebugKeystore(this)
                    println("Using default debug keystore")
                }
            } else {
                println("WARNING: Release keystore path not configured.")
                setupDebugKeystore(this)
                println("Using default debug keystore")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")

            isDebuggable = false
            isJniDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "API_BASE_URL", "\"https://api.production.com\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
        }

        // Build type khusus development
        create("development") {
            initWith(getByName("debug"))
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            matchingFallbacks += listOf("debug")

            // Nonaktifkan lint dan testing untuk build development
            enableUnitTestCoverage = false
            enableAndroidTestCoverage = false
        }


        debug {
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")

            buildConfigField("String", "API_BASE_URL", "\"https://api.staging.com\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")

            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }

        // Nonaktifkan untuk debug builds
        splits {
            abi {
                isEnable = false
            }
        }

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        // Enable desugaring untuk API levels yang lebih lama
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
        dataBinding = false
        aidl = false
        renderScript = false
        shaders = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/LICENSE*",
                "/META-INF/NOTICE*",
                "**/kotlin/**",
                "**/DebugProbesKt.bin",
                "META-INF/versions/9/previous-compilation-data.bin",
                "META-INF/*.version",
                "**/*.proto"
            )

            // Untuk debug, kurangin eksklusi untuk mempercepat
            pickFirsts += setOf(
                "**/META-INF/kotlin-stdlib-*.kotlin_module"
            )
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        disable += setOf("MissingTranslation", "ExtraTranslation")
        enable += setOf("ObsoleteLintCustomCheck", "GradleDependency")

        // Tambah lint check untuk target SDK
        checkOnly += setOf("NewApi", "InlinedApi", "OldTargetApi")
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    kotlinOptions {
        jvmTarget = "17"
        // Simplify compiler args
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

// Simple function tanpa generic issues
fun setupDebugKeystore(signingConfig: com.android.build.api.dsl.SigningConfig) {
    val debugKeystorePath = "${System.getProperty("user.home")}/.android/debug.keystore"
    val debugKeystoreFile = file(debugKeystorePath)

    if (debugKeystoreFile.exists()) {
        signingConfig.storeFile = debugKeystoreFile
        signingConfig.storePassword = "android"
        signingConfig.keyAlias = "androiddebugkey"
        signingConfig.keyPassword = "android"
        println("Fallback to debug keystore: ${debugKeystoreFile.absolutePath}")
    } else {
        println("ERROR: Debug keystore not found at: $debugKeystorePath")
    }
}

dependencies {
    // Hilt dependencies
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")

    // Core library desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Lifecycle (ViewModel, LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

    // WorkManager untuk background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // UI Components
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.activity:activity:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // Image Handling
    implementation("io.github.lucksiege:pictureselector:v3.11.2")
    implementation("io.github.lucksiege:ucrop:v3.11.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")

    // Utilities
    implementation("com.google.code.gson:gson:2.10.1")

    // Animate bottom navigation
    implementation("nl.joery.animatedbottombar:library:1.1.0")

    // AndroidX Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.7")

    // Shimmer for loading effects
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}

// Task untuk build analysis
tasks.register("analyzeBuild") {
    dependsOn("lintDebug", "testDebugUnitTest")
    doLast {
        println("Build analysis completed!")
    }
}

// Task untuk generate release notes secara otomatis
tasks.register("generateReleaseNotes") {
    doLast {
        val versionName = android.defaultConfig.versionName
        val versionCode = android.defaultConfig.versionCode
        val releaseNotes = """
            Version: $versionName ($versionCode)
            Build Date: ${LocalDateTime.now()}
            Build Type: Release
        """.trimIndent()

        val outputFile = file("${project.layout.buildDirectory}/outputs/release-notes.txt")
        outputFile.parentFile.mkdirs()
        outputFile.writeText(releaseNotes)

        println("Release notes generated: ${outputFile.absolutePath}")
    }
}

// Hook release notes generation setelah assemble
// tasks.named("assembleRelease") {
//     finalizedBy("generateReleaseNotes")
// }

// Task untuk check Android version compatibility
tasks.register("checkAndroidCompatibility") {
    doLast {
        println("Android Compatibility Check:")
        println("- compileSdk: ${android.compileSdk}")
        println("- targetSdk: ${android.defaultConfig.targetSdk}")
        println("- minSdk: ${android.defaultConfig.minSdk}")
        println("- Latest Android Version: Android 14 (API 34)")

        android.defaultConfig.targetSdk?.let {
            if (it < 34) {
                println("WARNING: Not targeting latest Android version. Consider updating targetSdk to 34.")
            } else {
                println("SUCCESS: Targeting latest Android version.")
            }
        }
    }
}
