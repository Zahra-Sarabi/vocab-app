import com.vocab.buildlogic.lib
import com.vocab.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Dependency injection. Android modules get full Hilt; pure Kotlin modules get Dagger's
 * annotation processor so their `@Inject` constructors are generated where they live.
 */
class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            dependencies {
                add("implementation", libs.lib("hilt-core"))
                add("ksp", libs.lib("dagger-compiler"))
            }
        }
        pluginManager.withPlugin("com.android.base") {
            pluginManager.apply("com.google.dagger.hilt.android")
            dependencies {
                add("implementation", libs.lib("hilt-android"))
                add("ksp", libs.lib("hilt-compiler"))
            }
        }
    }
}
