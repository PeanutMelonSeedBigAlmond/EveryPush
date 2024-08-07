import java.io.FileInputStream
import java.util.Calendar
import java.util.Properties
import java.util.TimeZone

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
//    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.plugin.compose")
    id("androidx.room")
}

val keyPropertiesFile = File(projectDir, "signing.properties")
val keyProperties = if (keyPropertiesFile.exists()) Properties() else null
keyProperties?.load(FileInputStream(keyPropertiesFile))


android {
    compileSdk = 34

    defaultConfig {
        applicationId = "moe.peanutmelonseedbigalmond.push"
        minSdk = 23
        targetSdk = 34
        versionCode = generateVersionCode()
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keyProperties != null) {
            maybeCreate("debug").apply {
                storeFile = File(keyProperties.getProperty("debug.keystoreFile"))
                storePassword = keyProperties.getProperty("debug.keystoreFilePassword")
                keyAlias = keyProperties.getProperty("debug.signingAlias")
                keyPassword = keyProperties.getProperty("debug.signingPassword")
            }
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
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

    productFlavors {
        flavorDimensions += "emoji"

        create("base") {
            versionNameSuffix = ".base"
            dimension = "emoji"
        }

        create("emoji") {
            versionNameSuffix = ".emoji"
            dimension = "emoji"
        }
    }

    splits {
        abi {
            isEnable = true
            isUniversalApk = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    kotlin {
        jvmToolchain(11)
    }

//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.4.8"
//    }
    room{
        schemaDirectory("$projectDir/schemas")
    }
    namespace = "moe.peanutmelonseedbigalmond.push"
}

val markwonVersion = "4.6.2"
val emojiVersion = "1.4.0"
var roomVersion="2.6.1"
dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.paging:paging-compose:3.3.0")

    //region Compose 组件
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.15.0")
    implementation("com.google.accompanist:accompanist-insets:0.15.0")
    implementation("com.google.accompanist:accompanist-insets-ui:0.15.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.7")
    //endregion

    //region Google 服务组件
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.firebaseui:firebase-ui-auth:7.2.0")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0-beta01")
    implementation("androidx.credentials:credentials:1.3.0-beta01")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
    //endregion

    //region 网络相关组件
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.4")
    implementation("com.squareup.okio:okio:3.8.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    //endregion

    //region Markdown 依赖
    implementation("io.noties.markwon:core:$markwonVersion")
    implementation("io.noties.markwon:ext-strikethrough:$markwonVersion")
    implementation("io.noties.markwon:ext-tables:$markwonVersion")
    implementation("io.noties.markwon:ext-tasklist:$markwonVersion")
    implementation("io.noties.markwon:html:$markwonVersion")
    implementation("io.noties.markwon:image:$markwonVersion")
    implementation("io.noties.markwon:image-coil:$markwonVersion")
    implementation("io.noties.markwon:linkify:$markwonVersion")
    implementation("io.noties.markwon:simple-ext:$markwonVersion")
    //endregion

    //region emoji 扩展支持
    "emojiImplementation"("androidx.emoji2:emoji2:$emojiVersion")
    "emojiImplementation"("androidx.emoji2:emoji2-bundled:$emojiVersion")
    //endregion

    implementation("com.github.DylanCaiCoding.Longan:longan:1.1.1")
    implementation("com.github.DylanCaiCoding:MMKV-KTX:1.2.16")

    // room
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // 其他
    implementation("com.github.nanchen2251:CompressHelper:1.0.5"){
        exclude("com.android.support","support-compat")
        exclude("com.android.support","support-media-compat")
    }
}

fun generateVersionCode(): Int {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"))
    val shortYear = calendar.get(Calendar.YEAR) % 100
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DATE)

    return try {
        return String.format("%02d%02d%02d", shortYear, month, day).toInt()
    } catch (_: Exception) {
        1
    }
}