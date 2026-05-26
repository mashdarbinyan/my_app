plugins {

    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

}

android {
    namespace = "mariam.darbinyan.login"
    compileSdk = 36 // Cleaned up syntax error here

    defaultConfig {
        applicationId = "mariam.darbinyan.login"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // NATIVE KOTLIN SOLUTION: Reads the file line-by-line without any special packages
        var discoveredKey = ""
        val propertiesFile = rootProject.file("local.properties")
        if (propertiesFile.exists()) {
            propertiesFile.readLines().forEach { line ->
                if (line.startsWith("picsart.api.key=")) {
                    // Extract everything after the '=' sign and clean up quotes/spaces
                    discoveredKey = line.substringAfter("=").trim().replace("\"", "")
                }
            }
        }

        // Securely inject it as a standard Java string field
        buildConfigField("String", "PICSART_API_KEY", "\"$discoveredKey\"")
    }

    buildFeatures {
        buildConfig = true // Cleaned up syntax to use '='
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth")

    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-database")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.google.guava:guava:33.0.0-android")
    implementation("com.google.android.gms:play-services-mlkit-subject-segmentation:16.0.0-beta1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")



    

}
