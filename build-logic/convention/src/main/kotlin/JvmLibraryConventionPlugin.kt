import com.vocab.buildlogic.configureKotlinJvm
import com.vocab.buildlogic.lib
import com.vocab.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** A pure Kotlin module with no Android dependency: models and domain logic. */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        configureKotlinJvm()
        dependencies {
            add("testImplementation", libs.lib("junit4"))
            add("testImplementation", libs.lib("kotlinx-coroutines-test"))
        }
    }
}
