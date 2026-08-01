import com.android.build.api.variant.impl.VariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    id("kotlin-parcelize")
    alias(libs.plugins.dependencyGuard)
    alias(libs.plugins.kotlinCompose)

    // for release
}

val applicationName = "VoiceMessageBoard"
val versionMajor = 1
val versionMinor = 12
val versionPatch = 5

android {
    namespace = "net.mm2d.android.vmb"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "net.mm2d.android.vmb"
        minSdk = 26
        targetSdk = 36
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
        base.archivesName.set("$applicationName-$versionName")
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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
    lint {
        abortOnError = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.outputs.forEach {
            (it as VariantOutputImpl).outputFileName.set("$applicationName-${it.versionName.get()}.apk")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugarJdkLibs)

    implementation(platform(libs.androidxComposeBom))
    implementation(libs.androidxActivityCompose)
    implementation(libs.androidxComposeUi)
    implementation(libs.androidxComposeUiGraphics)
    implementation(libs.androidxComposeUiToolingPreview)
    implementation(libs.androidxComposeMaterial3)
    implementation(libs.androidxComposeMaterialIcons)
    implementation(libs.androidxLifecycleRuntimeCompose)
    debugImplementation(libs.androidxComposeUiTooling)

    implementation(libs.kotlinxCoroutinesAndroid)
    implementation(libs.androidxAppCompat)
    implementation(libs.androidxPreferences)
    implementation(libs.androidxDataStorePreferences)
    implementation(libs.androidxCore)
    implementation(libs.androidxBrowser)
    implementation(libs.androidxLifecycleViewModel)
    implementation(libs.androidxLifecycleViewModelCompose)
    implementation(libs.androidxLifecycleProcess)
    implementation(libs.material)
    implementation(libs.playAppUpdate)

    // for release
}

dependencyGuard {
    configuration("releaseRuntimeClasspath")
}
