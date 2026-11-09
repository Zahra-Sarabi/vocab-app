plugins {
    alias(libs.plugins.vocab.android.library)
    alias(libs.plugins.vocab.hilt)
}

android {
    namespace = "com.vocab.core.database"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    api(projects.core.model)
    api(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
