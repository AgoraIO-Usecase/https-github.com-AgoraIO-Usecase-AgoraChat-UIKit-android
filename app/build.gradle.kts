import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val properties = Properties()
val inputStream = project.rootProject.file("local.properties").inputStream()
properties.load( inputStream )

android {
    namespace = "io.agora.chat.uikit.demo"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.agora.chat.uikit.demo"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField ("String", "APP_SERVER_PROTOCOL", "\"https\"")
        buildConfigField ("String", "APP_SERVER_DOMAIN", "\"a1.easemob.com\"")
        buildConfigField ("String", "APP_BASE_USER", "\"/inside/app/user/\"")
        buildConfigField ("String", "APP_SERVER_LOGIN", "\"login/V2\"")
        buildConfigField ("String", "APP_SERVER_REGISTER", "\"register\"")
        buildConfigField ("String", "APP_SERVE_CHECK_RESET", "\"reset/password\"")
        buildConfigField ("String", "APP_SERVE_CHANGE_PWD", "\"/password\"")
        buildConfigField ("String", "APP_SEND_SMS_FROM_SERVER", "\"/inside/app/sms/send\"")
        buildConfigField ("String", "APP_VERIFICATION_CODE", "\"/inside/app/image/\"")

        buildConfigField("String", "APPKEY", "\"${properties.getProperty("APPKEY")}\"")
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
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    applicationVariants.all {
        outputs.all { it ->
            val apkName = "easemob_demo_${buildType.name}_${versionName}.apk"
            if (it is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                it.outputFileName = apkName
            }
            true
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("io.github.scwang90:refresh-layout-kernel:2.1.0")
    implementation("io.github.scwang90:refresh-header-material:2.1.0")
    implementation("io.github.scwang90:refresh-header-classics:2.1.0")
    implementation("pub.devrel:easypermissions:3.0.0")
    // Coil: load image library
    implementation("io.coil-kt:coil:2.5.0")
    implementation(project(mapOf("path" to ":ease-im-kit")))
}