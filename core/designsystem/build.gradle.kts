plugins {
    alias(libs.plugins.vocab.android.library)
    alias(libs.plugins.vocab.android.compose)
}

android {
    namespace = "com.vocab.core.designsystem"
}

dependencies {
    api(projects.core.model)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.compose.ui)
}
