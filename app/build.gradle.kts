plugins {
    alias(libs.plugins.vocab.android.application)
    alias(libs.plugins.vocab.android.compose)
    alias(libs.plugins.vocab.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.vocab.app"

    defaultConfig {
        applicationId = "com.vocab.app"
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.data)
    implementation(projects.core.notifications)
    implementation(projects.feature.today)
    implementation(projects.feature.course)
    implementation(projects.feature.lesson)
    implementation(projects.feature.review)
    implementation(projects.feature.progress)
    implementation(projects.feature.settings)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.kotlinx.serialization.json)
}
