plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdk = 33
    defaultConfig {

        applicationId = "com.strizhonovapps_languages_learning.anylangapp_word_cards"
        vectorDrawables.useSupportLibrary = true
        minSdk = 24
        targetSdk = 33
        versionCode = 90
        versionName = "1.90"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val yandex_dictionary_api_key: String by project
        val unsplash_api_key: String by project
        resValue("string", "yandex_dictionary_api_key", yandex_dictionary_api_key)
        resValue("string", "unsplash_api_key", unsplash_api_key)
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildToolsVersion = "30.0.3"
}

repositories {
    mavenCentral()
    google()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.2")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.github.satyan:sugar:1.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("com.github.hotchemi:android-rate:1.0.1")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("androidx.work:work-runtime-ktx:2.7.1")

    implementation("com.google.dagger:dagger:2.42")
    implementation("com.google.dagger:dagger-android:2.42")
    implementation("com.google.dagger:dagger-android-support:2.42")
    kapt("com.google.dagger:dagger-compiler:2.42")
    kapt("com.google.dagger:dagger-android-processor:2.42")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.mockito:mockito-inline:4.6.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}