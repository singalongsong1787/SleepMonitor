pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()

        maven { url = uri("https://dl.google.com/dl/android/maven2/") }

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven { url = uri("https://jitpack.io") }
        maven { url= uri("https://chaquo.com/maven") }

    }
}

rootProject.name = "BNATest"
include(":app")