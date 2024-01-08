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

dependencies {
    api("li.cil.sedna:sedna:2.0.8")
    api("li.cil.sedna:sedna-buildroot:0.0.8")
    api("li.cil.ceres:ceres:0.0.4")

    api("it.unimi.dsi:fastutil:8.5.12")
    api("org.apache.commons:commons-lang3:3.14.0")
    api("commons-io:commons-io:2.15.1")
    api("org.apache.logging.log4j:log4j-core:2.22.1")

    api("org.ow2.asm:asm:9.6")
    api("org.ow2.asm:asm-commons:9.6")
    api("org.ow2.asm:asm-util:9.6")
    api("org.ow2.asm:asm-tree:9.6")
}