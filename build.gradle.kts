buildscript {
    val kotlinVersion: String by extra("1.3.72")
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.29.0")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}