/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package ink.bgp.asteroid.kotlinscript

import kotlinx.coroutines.runBlocking
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.*
import kotlin.script.experimental.dependencies.maven.MavenDependenciesResolver
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

object ScriptWithMavenDepsConfiguration : ScriptCompilationConfiguration(
    {
        // adds implicit import statements (in this case `import kotlin.script.experimental.dependencies.DependsOn`, etc.)
        // to each script on compilation
        defaultImports(DependsOn::class, Repository::class)

        jvm {
            // the dependenciesFromCurrentContext helper function extracts the classpath from current thread classloader
            // and take jars with mentioned names to the compilation classpath via `dependencies` key.
            // to add the whole classpath for the classloader without check for jar presence, use
            dependenciesFromCurrentContext(
                "internal-kotlin-script", // script library jar name
                "kotlin-scripting-dependencies", // DependsOn annotation is taken from this jar
                // wholeClasspath = true
            )
        }
        // section that callbacks during compilation
        refineConfiguration {
            // the callback called when any of the listed file-level annotations are encountered in the compiled script
            // the processing is defined by the `handler`, that may return refined configuration depending on the annotations
            onAnnotations(DependsOn::class, Repository::class, handler = ::configureMavenDepsOnAnnotations)
        }
    }
)

private val resolver = CompoundDependenciesResolver(FileSystemDependenciesResolver(), MavenDependenciesResolver())

// The handler that is called during script compilation in order to reconfigure compilation on the fly
fun configureMavenDepsOnAnnotations(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
    val annotations = context.collectedData?.get(ScriptCollectedData.collectedAnnotations)?.takeIf { it.isNotEmpty() }
        ?: return context.compilationConfiguration.asSuccess() // If no action is performed, the original configuration should be returned
    return runBlocking {
        // resolving maven artifacts using annotation arguments
        resolver.resolveFromScriptSourceAnnotations(annotations)
    }.onSuccess {
        context.compilationConfiguration.with {
            // updating the original configurations with the newly resolved artifacts as compilation dependencies
            dependencies.append(JvmDependency(it))
        }.asSuccess()
    }
}

