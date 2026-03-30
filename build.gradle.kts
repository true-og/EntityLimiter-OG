/* This is free and unencumbered software released into the public domain */

plugins {
    id("java")
    id("java-library")
    id("com.diffplug.spotless") version "8.1.0"
    id("com.gradleup.shadow") version "8.3.9"
    id("checkstyle")
    eclipse
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.GRAAL_VM)
    }
}

group = "jukerx"

version = "1.5"

val pluginName = "EntityLimiter-OG"
val apiVersion = "1.19"

tasks.named<ProcessResources>("processResources") {
    val props = mapOf("name" to pluginName, "version" to version, "apiVersion" to apiVersion)
    inputs.properties(props)
    filesMatching("plugin.yml") { expand(props) }
    from("LICENSE") { into("/") }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://repo.purpurmc.org/snapshots") }
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.shadowJar {
    archiveBaseName.set(pluginName)
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())
    relocate("org.bstats", "jukerx.entitylimiter.shaded.bstats")
    minimize()
}

tasks.jar {
    archiveBaseName.set(pluginName)
    archiveClassifier.set("part")
    archiveVersion.set(project.version.toString())
}

tasks.build { dependsOn(tasks.spotlessApply, tasks.shadowJar) }

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.isFork = true
    options.compilerArgs.add("-Xlint:deprecation")
    options.encoding = "UTF-8"
}

spotless {
    java {
        eclipse().configFile("config/formatter/eclipse-java-formatter.xml")
        leadingTabsToSpaces()
        removeUnusedImports()
        target("src/**/*.java")
    }
    kotlinGradle {
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
        target("build.gradle.kts", "settings.gradle.kts")
    }
}

checkstyle {
    toolVersion = "10.18.1"
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = true
    isShowViolations = true
}

tasks.named("compileJava") { dependsOn("spotlessApply") }

tasks.named("spotlessCheck") { dependsOn("spotlessApply") }
