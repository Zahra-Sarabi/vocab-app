plugins {
    alias(libs.plugins.vocab.android.library)
    alias(libs.plugins.vocab.hilt)
}

android {
    namespace = "com.vocab.core.notifications"
}

dependencies {
    implementation(projects.core.domain)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
}
