rootProject.name = "asteroid"
include("internal")
include("jarinjar-handler")
include("api")

include("internal-javadowngrader")
include("internal-javaupgrader")


include("fun-sedna")
include("fun-sedna:launcher")

private var scanProject:(ProjectDescriptor) -> Unit = {}

scanProject = { projectDescriptor ->
    projectDescriptor.children.forEach(scanProject)

    if (projectDescriptor.path != ":") {
        projectDescriptor.name = rootProject.name + projectDescriptor.path.replace(':', '-')
    }
}

scanProject(rootProject)
