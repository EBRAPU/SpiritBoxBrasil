plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.ebrapu.spiritboxbrasil"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ebrapu.spiritboxbrasil"
        minSdk = 24
        targetSdk = 34
        versionCode = 5 // Atualize para o próximo número inteiro
        versionName = "3.2" // Atualize para a nova versão visível

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Habilita a ofuscação
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ExoPlayer
    implementation("com.google.android.exoplayer:exoplayer-core:2.18.1") // Use a versão mais recente

    // Adicione a dependência do NotificationCompat
    implementation("androidx.core:core:1.9.0") // Use a versão mais recente disponível
}
