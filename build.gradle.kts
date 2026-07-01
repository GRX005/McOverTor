plugins {
    id("net.fabricmc.fabric-loom")
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("mod_group").get()

base {
    archivesName = providers.gradleProperty("mod_base_name")
}

repositories {
    // Add repositories to retrieve artifacts from in here.
}

dependencies {
    val fabVer = providers.gradleProperty("fabric_api_version").get()
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")

    implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

    include(implementation("io.netty:netty-handler-proxy:4.2.15.Final")!!)
    include(implementation("io.netty:netty-codec-socks:4.2.15.Final")!!)

    include(implementation(fabricApi.module("fabric-resource-loader-v1", fabVer))!!)
    include(implementation(fabricApi.module("fabric-api-base", fabVer))!!)
}

tasks.processResources {
    val modVersion = project.version.toString()

    inputs.property("version", modVersion)

    filesMatching("fabric.mod.json") {
        expand("version" to modVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

// set the archive base name using modern Jar property API (avoids deprecated base/archive properties)
tasks.jar {
    // 2. Declare the provider LOCALLY inside the task block
    val archiveNameProvider = base.archivesName

    inputs.property("archivesName", archiveNameProvider)

    from("LICENSE") {
        rename { "${it}_${archiveNameProvider.get()}" }
    }
}