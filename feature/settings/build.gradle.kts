plugins {
    alias(libs.plugins.vocab.android.feature)
}

android {
    namespace = "com.vocab.feature.settings"
}

dependencies {
    implementation(libs.androidx.activity.compose)
}
