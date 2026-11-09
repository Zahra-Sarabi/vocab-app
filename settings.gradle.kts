pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "VocabApp"

include(":app")

include(":core:model")
include(":core:domain")
include(":core:designsystem")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":core:notifications")

include(":feature:today")
include(":feature:course")
include(":feature:lesson")
include(":feature:review")
include(":feature:progress")
include(":feature:settings")
