plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "com.artillexstudios.axvouchers"
version = "1.0.0"

repositories {
    mavenCentral()

    maven("https://repo.artillex-studios.com/releases/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    implementation("com.artillexstudios.axapi:axapi:2.1.0-DEV-52:all")
    implementation("dev.jorel:commandapi-paper-shade:11.2.0")
    compileOnly("dev.triumphteam:triumph-gui:3.1.7")
    compileOnly("com.h2database:h2:2.3.232")
    compileOnly("com.zaxxer:HikariCP:5.0.1")
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("org.apache.commons:commons-math3:3.6.1")
    compileOnly("org.apache.commons:commons-text:1.11.0")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("commons-io:commons-io:2.15.0")
    compileOnly("it.unimi.dsi:fastutil:8.5.13")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.add("-parameters")
        options.isFork = true
    }

    shadowJar {
        relocate("org.apache.commons.math3", "com.artillexstudios.axvouchers.libs.axapi.libs.math3")
        relocate("com.artillexstudios.axapi", "com.artillexstudios.axvouchers.libs.axapi")
        relocate("dev.jorel.commandapi", "com.artillexstudios.axvouchers.libs.commandapi")
        relocate("dev.triumphteam", "com.artillexstudios.axvouchers.libs.triumphgui")
        relocate("com.zaxxer", "com.artillexstudios.axvouchers.libs.hikaricp")
        relocate("org.h2", "com.artillexstudios.axvouchers.libs.h2")
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()

        filesMatching("plugin.yml") {
            expand(mapOf("version" to project.version))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}
