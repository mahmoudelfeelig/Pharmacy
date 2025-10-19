pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Pharmacy"
include(
    ":app",
    ":core-domain",
    ":core-data",
    ":feature-auth",
    ":feature-home",
    ":feature-profile",
    ":feature-map"
)
