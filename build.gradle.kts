plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "com.artillexstudios.axvouchers"
version = "1.0.0"

repositories {
    mavenCentral()

    maven("https://repo.artillex-studios.com/releases/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    implementation("com.artillexstudios.axapi:axapi:1.4.214:all")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("dev.triumphteam:triumph-gui:3.1.7")
    compileOnly("com.h2database:h2:2.2.220")
    compileOnly("org.xerial:sqlite-jdbc:3.42.0.0")
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
    compileOnly("commons-io:commons-io:2.15.0")
    compileOnly("org.apache.commons:commons-text:1.11.0")
    compileOnly("org.apache.commons:commons-math3:3.6.1")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("it.unimi.dsi:fastutil:8.5.13")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.add("-parameters")
        options.isFork = true
    }

    shadowJar {
        relocate("com.artillexstudios.axapi", "com.artillexstudios.axvouchers.libs.axapi")
        relocate("org.sqlite", "com.artillexstudios.axvouchers.libs.sqlite")
        relocate("org.h2", "com.artillexstudios.axvouchers.libs.h2")
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()

        filesMatching("plugin.yml") {
            expand(mapOf("version" to project.version,))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}
