plugins {
    alias(libs.plugins.vocab.android.library)
    alias(libs.plugins.vocab.hilt)
}

android {
    namespace = "com.vocab.core.datastore"
}

dependencies {
    api(projects.core.model)
    implementation(libs.androidx.datastore.preferences)
}
