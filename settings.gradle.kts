pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // required for Android & Kotlin plugins
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
    }
}

rootProject.name = "FansubCreator"
include(":app")