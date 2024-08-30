import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
    id("de.mannodermaus.android-junit5")
    id("kotlin-kapt")
}

android {
    signingConfigs {
        create("release") {
            val props = Properties()
            val fileInputStream = FileInputStream(file("../signing.properties"))
            props.load(fileInputStream)
            fileInputStream.close()

            storeFile = file(props["storeFilePath"] as String)
            storePassword = props["storePassword"] as String
            keyPassword = props["keyPassword"] as String
            keyAlias = props["keyAlias"] as String
        }
    }

    namespace = "com.heyzeusv.plutuswallet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.heyzeusv.plutuswallet"
        minSdk = 21
        targetSdk = 34
        versionCode = 29
        versionName = "4.1.1"
        testInstrumentationRunnerArguments["runnerBuilder"] =
            "de.mannodermaus.junit5.AndroidJUnit5Builder"
        testInstrumentationRunner = "com.heyzeusv.plutuswallet.CustomTestRunner"
        resourceConfigurations.addAll(listOf("en", "es", "de", "hi", "ja", "ko", "th"))
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".dev"
            isPseudoLocalesEnabled = true
        }
    }
    buildFeatures {
        compose = true
        dataBinding = true
    }
    compileOptions {
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
    packagingOptions {
        jniLibs {
            excludes += listOf("META-INF/LICENSE*")
        }
        resources {
            excludes += listOf("META-INF/LICENSE*", "META-INF/AL2.0", "META-INF/LGPL2.1")
        }
    }
    testOptions {
        animationsDisabled = true
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.0") {
            because("kotlin-stdlib-jdk7 is now a part of kotlin-stdlib")
        }
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0") {
            because("kotlin-stdlib-jdk8 is now a part of kotlin-stdlib")
        }
    }

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.activity:activity-compose:1.6.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.2")
    // Accompanist
    implementation("com.google.accompanist:accompanist-flowlayout:0.30.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.30.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.0")
    // Compose
    implementation("androidx.compose.animation:animation:1.4.0")
    implementation("androidx.compose.foundation:foundation:1.4.0")
    implementation("androidx.compose.foundation:foundation-layout:1.4.0")
    implementation("androidx.compose.material:material:1.4.0")
    implementation("androidx.compose.material:material-icons-extended:1.4.0")
    implementation("androidx.compose.material3:material3:1.1.0-beta01")
    implementation("androidx.compose.material3:material3-window-size-class:1.1.0-beta01")
    implementation("androidx.compose.runtime:runtime:1.4.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.4.0")
    implementation("androidx.compose.ui:ui:1.4.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")
    // CoRoutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    // Hilt
    implementation("com.google.dagger:hilt-android:2.42")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    kapt("com.google.dagger:hilt-compiler:2.42")
    kapt("androidx.hilt:hilt-compiler:1.0.0-alpha03")
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    // Material Design
    implementation("com.google.android.material:material:1.9.0-beta01")
    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    // Preferences
    implementation("androidx.preference:preference-ktx:1.2.0")
    // Room API
    implementation("androidx.room:room-runtime:2.5.1")
    implementation("androidx.room:room-ktx:2.5.1")
    kapt("androidx.room:room-compiler:2.5.1")
    // Timber
    implementation("com.jakewharton.timber:timber:4.7.1")

    // Dependencies for local unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    testImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    testImplementation("com.google.truth:truth:1.0")

    // AndroidX Test - JVM testing
    testImplementation("androidx.test:core-ktx:1.5.0")
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")
    testImplementation("androidx.test:rules:1.5.0")
    testImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:core:1.5.0")

    // Dependencies for Android unit tests
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    // AndroidX Test - Instrumented testing
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.room:room-testing:2.5.1")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test.espresso.idling:idling-concurrent:3.5.1")
    implementation("androidx.test.espresso:espresso-idling-resource:3.5.1")
    androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.3.0")
    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.3.0")
    androidTestImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    // Compose testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.4.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.4.0")
    // Hilt testing
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.42")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.42")
    kaptAndroidTest("androidx.hilt:hilt-compiler:1.0.0-alpha03")
    // Navigation testing
    androidTestImplementation("androidx.navigation:navigation-testing:2.5.3")

    // Resolve conflicts between main and test APK:
    //noinspection GradleDependency SDK 33
    androidTestImplementation("androidx.annotation:annotation:1.1.0")
    androidTestImplementation("androidx.legacy:legacy-support-v4:1.0.0")
    androidTestImplementation("androidx.appcompat:appcompat:1.6.1")
    androidTestImplementation("com.google.android.material:material:1.9.0-beta01")
}
