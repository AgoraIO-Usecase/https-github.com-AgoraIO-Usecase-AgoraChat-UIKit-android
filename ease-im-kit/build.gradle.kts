plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "io.agora.chat.uikit"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures{
        viewBinding = true
    }
    sourceSets {
        getByName("main") {
            java.srcDir("src/main/kotlin")
            //res.srcDirs("src/main/res", "src/main/res-circle")
            res.srcDirs("src/main/res", "src/main/res-round")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.9.0")

    implementation("com.belerweb:pinyin4j:2.5.0")

    // Activity for kotlin
    implementation("androidx.activity:activity-ktx:1.8.0")
    // Fragment for kotlin
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    // lifecycle viewmodel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    // coroutines core library
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    // coroutines android library
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // Coil: load image library
    implementation("io.coil-kt:coil:2.5.0")
    // Refresh layout
    implementation("io.github.scwang90:refresh-layout-kernel:2.1.0")
    implementation("io.github.scwang90:refresh-header-material:2.1.0")
    // Chat SDK
	api("io.agora.rtc:chat-sdk:1.3.1")
//    implementation(project(mapOf("path" to ":hyphenatechatsdk")))
}

//apply {from("../script/maven-push-release.gradle")}
//apply {from("../script/maven-push-shengwang-release.gradle")}
//apply {from("../script/maven-push-agora-release.gradle")}