import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("kotlin-parcelize")
    alias(libs.plugins.gradleVersions)
    alias(libs.plugins.dependencyGuard)

    // for release
}

val applicationName = "VoiceMessageBoard"
val versionMajor = 1
val versionMinor = 11
val versionPatch = 9

android {
    compileSdk = 35

    namespace = "net.mm2d.android.vmb"
    defaultConfig {
        applicationId = "net.mm2d.android.vmb"
        minSdk = 26
        targetSdk = 35
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
        base.archivesName.set("$applicationName-$versionName")
    }
    applicationVariants.all {
        if (buildType.name == "release") {
            outputs.all {
                (this as BaseVariantOutputImpl).outputFileName = "$applicationName-$versionName.apk"
            }
        }
    }
    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "d"
            enableAndroidTestCoverage = true
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
        jvmToolchain(17)
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    lint {
        abortOnError = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.kotlinStdlib)
    implementation(libs.kotlinxCoroutinesAndroid)

    implementation(libs.androidxAppCompat)
    implementation(libs.androidxPreferences)
    implementation(libs.androidxCore)
    implementation(libs.androidxBrowser)
    implementation(libs.androidxLifecycleViewModel)
    implementation(libs.androidxLifecycleProcess)
    implementation(libs.material)
    implementation(libs.playAppUpdate)

    debugImplementation(libs.leakcanary)
    debugImplementation(libs.bundles.flipper)

    // for release
}

dependencyGuard {
    configuration("releaseRuntimeClasspath")
}

fun isStable(
    version: String,
): Boolean {
    val versionUpperCase = version.uppercase()
    val hasStableKeyword = listOf("RELEASE", "FINAL", "GA").any { versionUpperCase.contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return hasStableKeyword || regex.matches(version)
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    rejectVersionIf { !isStable(candidate.version) && isStable(currentVersion) }
}
