import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("com.github.ben-manes.versions")
}

val applicationName = "VoiceMessageBoard"
val versionMajor = 1
val versionMinor = 8
val versionPatch = 0

android {
    compileSdkVersion(30)
    buildToolsVersion = "29.0.3"

    defaultConfig {
        applicationId = "net.mm2d.android.vmb"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
        base.archivesBaseName = "${applicationName}-${versionName}"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
    }
    buildTypes {
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "d"
        }
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    applicationVariants.all {
        if (buildType.name == "release") {
            outputs.all {
                (this as BaseVariantOutputImpl).outputFileName = "${applicationName}-${versionName}.apk"
            }
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.preference:preference:1.1.1")
    implementation("androidx.core:core-ktx:1.3.1")
    implementation("androidx.browser:browser:1.2.0")
    implementation("androidx.lifecycle:lifecycle-process:2.2.0")
    implementation("com.google.android.material:material:1.2.1")
    implementation("com.google.android.play:core:1.8.0")
    implementation("com.google.android.play:core-ktx:1.8.1")
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    testImplementation("junit:junit:4.13")
}

fun isStable(version: String): Boolean {
    val versionUpperCase = version.toUpperCase()
    val hasStableKeyword = listOf("RELEASE", "FINAL", "GA").any { versionUpperCase.contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return hasStableKeyword || regex.matches(version)
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    rejectVersionIf { !isStable(candidate.version) }
}
