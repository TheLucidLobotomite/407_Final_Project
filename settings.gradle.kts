pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
<<<<<<< HEAD
=======

    }
    plugins {
        id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"
>>>>>>> 5116f58 (adding my device files)
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

<<<<<<< HEAD
rootProject.name = "BrickCollector"
include(":app")
 
=======
rootProject.name = "Location"
include(":app")
>>>>>>> 5116f58 (adding my device files)
