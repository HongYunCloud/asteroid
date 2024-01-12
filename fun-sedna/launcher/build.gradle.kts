java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    val configureFnueckeRepository:(String)->Unit = { moduleName ->
        maven("https://maven.pkg.github.com/fnuecke/$moduleName") {
            credentials {
                username = (project.findProperty("gpr.user")?: System.getenv("USERNAME"))
                    ?.toString()
                password = (project.findProperty("gpr.key") ?: System.getenv("TOKEN"))
                    ?.toString()
            }
            content {
                includeModuleByRegex("^li\\.cil.+\$", "^${Regex.escape(moduleName)}\$")
            }
        }
    }
    configureFnueckeRepository("sedna")
    configureFnueckeRepository("sedna-buildroot")
    configureFnueckeRepository("ceres")
}

configurations {
    configurations.create("shadow")
    compileOnly {
        extendsFrom(configurations["shadow"])
    }
}

dependencies {
    "shadow"(project(":")) {
        exclude("io.github.hongyuncloud", "asteroid-internal")
    }
    "shadow"("org.apache.logging.log4j:log4j-core:2.22.1")
    "shadow"("org.apache.logging.log4j:log4j-jul:2.22.1")

    runtimeOnly(project(":fun-sedna"))
}

tasks.processResources {
    dependsOn(project(":").tasks.jar)

    with(copySpec{
        from(configurations["shadow"].map {
            if (!it.isDirectory && it.extension == "jar") {
                zipTree(it)
            } else {
                it
            }
        })

        exclude("META-INF/MANIFEST.MF")

        duplicatesStrategy = DuplicatesStrategy.WARN
    })

    with(copySpec{
        from(configurations.runtimeClasspath)
        into("ASTEROID-LIBS")

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    })
}

tasks.jar {
    manifest {
        attributes["Multi-Release"] = "true"
        attributes["Main-Class"] = "ink.bgp.asteroid.loader.AsteroidMain"
        attributes["Launcher-Agent-Class"] = "ink.bgp.asteroid.loader.AsteroidMain"
    }

    entryCompression = ZipEntryCompression.STORED
}