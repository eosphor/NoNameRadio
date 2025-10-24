import com.android.build.gradle.api.ApkVariantOutput
import de.undercouch.gradle.tasks.download.Download
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.configureEach

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.download.task)
    kotlin("kapt")
}

fun Project.collectAvailableLocales(): List<String> {
    val tree = fileTree(mapOf("dir" to "src/main/res", "include" to listOf("**/strings.xml")))
    return tree.map { file ->
        var lang = file.parentFile?.name.orEmpty()
        lang = lang.removePrefix("values-")
        if (lang == "values") {
            lang = ""
        }
        lang = lang.replace("-r", "-")
        if (lang.isEmpty()) "en" else lang
    }
}

android {
    namespace = "com.nonameradio.app"
    compileSdk = 35
    flavorDimensions += "one"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of("17"))
        }
    }

    buildFeatures {
        viewBinding = true
        aidl = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.nonameradio.app"
        minSdk = 26
        targetSdk = 35

        versionCode = 100
        versionName = "0.87.0"

        vectorDrawables.useSupportLibrary = true

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = project.file("schemas").toString()
            }
        }

        resValue("string", "app_name_untranslated", "NoNameRadio")

        val escapedLocales = collectAvailableLocales().joinToString(",") { "\"$it\"" }
        buildConfigField("String[]", "AVAILABLE_LOCALES", "{ $escapedLocales }")

        testInstrumentationRunner = "com.nonameradio.app.tests.CustomTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"

        buildConfigField("boolean", "IS_TESTING", "false")

        multiDexKeepProguard = file("multidex-config.pro")
        vectorDrawables.useSupportLibrary = true
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"

        unitTests {
            isReturnDefaultValues = true
            all {
                it.useJUnit()
            }
        }
        animationsDisabled = true
    }

    sourceSets {
        getByName("androidTest") {
            resources.srcDir("src/androidTest/resources")
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    productFlavors {
        create("play") {
            dimension = "one"
            // Google Cast available only in Play flavor
        }
        create("free") {
            dimension = "one"
        }
    }

    lint {
        abortOnError = true
        disable += listOf("UnsafeOptInUsageError", "MissingTranslation", "NullSafeMutableLiveData")
        checkReleaseBuilds = false  // Temporarily disabled due to lint detector crash
        warningsAsErrors = false
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
}

android.applicationVariants.configureEach {
    val baseName = buildString {
        append(mergedFlavor.resValues["string/app_name_untranslated"]?.value ?: "")
        productFlavors.forEach { flavor ->
            append("-${flavor.name}")
        }
        append("-${buildType.name}")
        if (buildType.name == "debug") {
            append("-DEV")
        }
    }

    val gitHash = runCatching {
        val stdout = ByteArrayOutputStream()
        project.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim()
    }.getOrDefault("No commit hash")

    val buildDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

    resValue("string", "GIT_HASH", gitHash)
    resValue("string", "BUILD_DATE", buildDate)

    val versionName = versionName
    val finalName = buildString {
        append(baseName)
        if (!versionName.isNullOrEmpty()) {
            append("-")
            append(versionName)
        }
        append("-")
        append(gitHash)
        append(".apk")
    }

    outputs.configureEach {
        if (this is ApkVariantOutput) {
            outputFileName = finalName
        }
    }
}

dependencies {
    // Core AndroidX
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.viewpager2)
    implementation(libs.material)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.mediarouter)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.tvprovider)
    implementation(libs.androidx.tracing)

    // Networking
    implementation(libs.okhttp)
    implementation(libs.gson)

    // Media
    implementation(libs.bundles.media3)

    // Image loading
    implementation(libs.bundles.glide)
    kapt(libs.glide.compiler)

    // UI Components
    implementation(libs.iconics.core)
    implementation(libs.iconics.views)
    implementation(libs.google.material.typeface)
    implementation(libs.community.material.typeface)

    // Utilities
    implementation(libs.file.dialogs)
    implementation(libs.java.string.similarity)
    implementation(libs.fuzzywuzzy)
    implementation(libs.material.popup.menu)
    implementation(libs.search.preference)

    // Database
    implementation(libs.room.runtime)
    implementation(libs.room.paging)
    kapt(libs.room.compiler)

    // Lifecycle
    implementation(libs.lifecycle.common.java8)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Paging
    implementation(libs.paging.runtime.ktx)

    // Analytics
    implementation(libs.yandex.metrica)

    // Google Cast (used in play flavor sources)
    implementation(libs.play.services.cast.framework)

    // Testing
    testImplementation(libs.junit4)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.uiautomator)
    androidTestImplementation(libs.espresso.contrib) {
        exclude(group = "org.checkerframework", module = "checker")
    }
    androidTestImplementation(libs.mockwebserver)
    androidTestImplementation(libs.lingver)
    androidTestUtil(libs.androidx.test.orchestrator)

    // Mockito for unit tests
    testImplementation(libs.mockito.core)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.mockito.android)

    // Memory leak detection (debug only) - LeakCanary v3.0-alpha-8
    debugImplementation("com.squareup.leakcanary:leakcanary-object-watcher-android:3.0-alpha-8")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

tasks.register<Download>("renewFallbackStations") {
    group = "Build Setup"
    description = "Renews the list of some popular and recently checked fallback stations"
    src("https://de1.api.radio-browser.info/json/stations/search?limit=10&bitrateMax=128&hidebroken=true&has_extended_info=true&order=lastchecktime&reverse=true&bitrateMax=128")
    dest(File(projectDir, "src/main/res/raw/fallback_stations.json"))
    overwrite(true)
    tempAndMove(true)
}
