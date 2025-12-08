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
        // Linphone SDK repository (required for SIP integration)
        maven { url = uri("https://linphone.org/maven_repository/") }
    }
}
rootProject.name = "Pharmacy"
include(
    ":app",
    ":core-domain",
    ":core-data",
    ":core-ui",
    ":feature-auth",
    ":feature-home",
    ":feature-profile",
    ":feature-map"
)
include(":core-ui")
