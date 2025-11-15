plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.api.ruletaeuropea"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.api.ruletaeuropea"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        // Campos de calibración expuestos para cambiar sin tocar código
        buildConfigField("Float", "ROULETTE_ASSET_OFFSET_DEG", "4.8649f") // valor inicial, ajustar en futuras builds
        buildConfigField("Boolean", "ROULETTE_ASSET_CCW", "false")
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true // habilitado explícitamente para permitir buildConfigField
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-compose:1.8.0")


    // Compose - usar Compose BOM para gestionar versiones
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    // Añadido: foundation para KeyboardOptions (ubicado en foundation.text)
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation") // añadido para AnimatedVisibility

    implementation("com.airbnb.android:lottie-compose:6.0.0")

    val navVersion = "2.8.2"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    implementation("androidx.navigation:navigation-compose:2.8.2")


    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Necesario para Icons.Filled.Visibility / VisibilityOff
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    // Test unitarios
    testImplementation("junit:junit:4.13.2")
}
