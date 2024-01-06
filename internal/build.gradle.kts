dependencies {
    api(project(":asteroid-jarinjar-handler"))
    api(project(":asteroid-api"))

    api("org.ow2.asm:asm:9.6")

    api("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:1.7.36")

    api("org.apache.ivy:ivy:2.5.2")
}