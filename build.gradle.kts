plugins {
    java
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT"
    id("com.gradleup.shadow") version "9.0.0-beta13"
    id("maven-publish")
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val targetJavaVersion = 25
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    withSourcesJar()
}

loom {
    mods {
        register("dvcbridge") {
            sourceSet("main")
        }
    }
}

repositories {
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content { includeGroup("maven.modrinth") }
    }
    maven {
        name = "Henkelmax"
        url = uri("https://maven.maxhenkel.de/repository/public")
    }
    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
    }
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    compileOnly("de.maxhenkel.voicechat:voicechat-api:${project.property("voicechat_api_version")}")
    runtimeOnly("maven.modrinth:simple-voice-chat:${project.property("voicechat_version")}")

    implementation("org.bspfsystems:yamlconfiguration:${project.property("yaml_config_version")}")
    shadow("org.bspfsystems:yamlconfiguration:${project.property("yaml_config_version")}")
}

tasks.shadowJar {
    configurations = listOf(project.configurations.getByName("shadow"))
    relocate("org.bspfsystems.yamlconfiguration", "fortniop.dvcbridge.shadow.yamlconfiguration")
    relocate("org.yaml.snakeyaml", "fortniop.dvcbridge.shadow.snakeyaml")
    exclude("org/slf4j/**")
    archiveClassifier.set("deps")
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    from(zipTree(tasks.shadowJar.get().archiveFile))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version")!!,
            "loader_version" to project.property("loader_version")!!
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }
}
