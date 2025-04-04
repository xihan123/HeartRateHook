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
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://jitpack.io") {
            content {
                includeGroupByRegex("com\\.github.*")
            }
        }
        google()
        mavenCentral()
        maven("https://api.xposed.info/") {
            mavenContent {
                includeGroup("de.robv.android.xposed")
            }
        }
    }
}

rootProject.name = "HeartRateHook"
include(":app")
