plugins {
    alias(libs.plugins.vocab.jvm.library)
    alias(libs.plugins.vocab.hilt)
}

dependencies {
    api(projects.core.model)
    api(libs.kotlinx.coroutines.core)
}
