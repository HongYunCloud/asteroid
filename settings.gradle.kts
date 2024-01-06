rootProject.name = "asteroid"
include("internal")
include("jarinjar-handler")
include("api")

include("fun-sedna")


private var scanProject:(ProjectDescriptor) -> Unit = {}

scanProject = { projectDescriptor ->
    projectDescriptor.children.forEach(scanProject)

    if (projectDescriptor.path != ":") {
        projectDescriptor.name = rootProject.name + projectDescriptor.path.replace(':', '-')
    }
}

scanProject(rootProject)
