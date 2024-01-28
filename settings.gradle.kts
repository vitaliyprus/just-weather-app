pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "Just Weather App"

include(":app")
include(":core")
include(":domain:home")
include(":data:home")
include(":theme")
include(":feature:home")
include(":feature:locations")
include(":feature:weather")
include(":feature:settings")
