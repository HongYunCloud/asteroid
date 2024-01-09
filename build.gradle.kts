plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = "io.github.hongyuncloud"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://maven.lenni0451.net/releases") {
            content {
                includeGroup("net.raphimc.javadowngrader")
            }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

        withSourcesJar()
    }

    publishing {
        repositories {
            if (System.getenv("CI").toBoolean()) {
                maven("https://r.bgp.ink/maven/") {
                    credentials {
                        username = System.getenv("R_BGP_INK_USERNAME")
                        password = System.getenv("R_BGP_INK_PASSWORD")
                    }
                }
            } else {
                maven(rootProject.layout.buildDirectory.dir("maven"))
            }
        }

        publications {
            create<MavenPublication>("mavenJar") {
                from(components["java"])
            }
        }
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.26")
        annotationProcessor("org.projectlombok:lombok:1.18.26")

        compileOnly("org.jetbrains:annotations:24.1.0")

        compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
        annotationProcessor("com.google.auto.service:auto-service:1.1.1")

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
        attributes["Can-Retransform-Classes"] = "true"
        attributes["Can-Redefine-Classes"] = "true"
        attributes["Can-Set-Native-Method-Prefix"] = "true"
    }

    entryCompression = ZipEntryCompression.STORED
}