plugins {
    alias(libs.plugins.vocab.android.library)
    alias(libs.plugins.vocab.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.vocab.core.data"
}

dependencies {
    api(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
}
