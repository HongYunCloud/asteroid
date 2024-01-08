plugins {
    id("java")
    id("java-library")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    group = "io.github.hongyuncloud"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

        withSourcesJar()
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.26")
        annotationProcessor("org.projectlombok:lombok:1.18.26")

        compileOnly("org.jetbrains:annotations:24.1.0")

        testImplementation(platform("org.junit:junit-bom:5.9.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks.test {
        useJUnitPlatform()
    }
}

configurations {
    configurations.create("shadow")
    compileOnly {
        extendsFrom(configurations["shadow"])
    }
}

dependencies {
    "shadow"(project(":asteroid-jarinjar-handler"))
    "shadow"(project(":asteroid-api"))

    runtimeOnly(project(":asteroid-internal")) {
        exclude(group = group, module = "asteroid-jarinjar-handler")
        exclude(group = group, module = "asteroid-api")
    }
}

tasks.processResources {
    dependsOn(project(":asteroid-jarinjar-handler").tasks.jar)
    dependsOn(project(":asteroid-api").tasks.jar)

    with(copySpec{
        from(configurations.runtimeClasspath)
        into("ASTEROID-LIBS")
    })

    with(copySpec{
        from(configurations["shadow"].map {
            if (!it.isDirectory && it.extension == "jar") {
                zipTree(it)
            } else {
                it
            }
        })
        exclude("META-INF/MANIFEST.MF")
    })
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "ink.bgp.asteroid.loader.AsteroidMain"
        attributes["Launcher-Agent-Class"] = "ink.bgp.asteroid.loader.AsteroidMain"
    }

    entryCompression = ZipEntryCompression.STORED
}