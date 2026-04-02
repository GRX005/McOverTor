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

//dependencies {
//    // To change the versions see the gradle.properties file
//    mappings loom.officialMojangMappings()
//    include(modImplementation("io.netty:netty-handler-proxy:4.2.10.Final"))
//    include(modImplementation("io.netty:netty-codec-socks:4.2.10.Final"))
//    minecraft("com.mojang:minecraft:${cfgMinecraftVersion}")
//
//    modImplementation("net.fabricmc:fabric-loader:${cfgLoaderVersion}")
//    include(modImplementation(fabricApi.module("fabric-resource-loader-v1","${project.fabric_api_version}")))
//    include(modImplementation(fabricApi.module("fabric-api-base", "${project.fabric_api_version}")))
//}

dependencies {
    val fabVer = providers.gradleProperty("fabric_api_version").get()
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")

    implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

    include(implementation("io.netty:netty-handler-proxy:4.2.12.Final")!!)
    include(implementation("io.netty:netty-codec-socks:4.2.12.Final")!!)

    include(implementation(fabricApi.module("fabric-resource-loader-v1", fabVer))!!)
    include(implementation(fabricApi.module("fabric-api-base", fabVer))!!)

}

tasks.processResources {
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("version" to version)
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
    inputs.property("archivesName", base.archivesName)

    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}