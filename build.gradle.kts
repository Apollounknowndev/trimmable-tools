plugins {
    kotlin("jvm") version "2.1.0"
    id("earth.terrarium.cloche") version "0.17.6"
}

repositories {
    cloche {
        mavenNeoforgedMeta()
        mavenNeoforged()
        mavenForge()
        mavenFabric()
        mavenParchment()
        librariesMinecraft()
        main()
    }
    mavenCentral()
    maven("https://api.modrinth.com/maven")
}

group = "dev.worldgen.trimmable.tools"
version = "2.0.7"

cloche {
    mappings {
        official()
    }

    metadata {
        modId = "trimmable_tools"
        name = "Trimmable Tools"
        description = "Trim your tools!"
        license = "MIT"
        icon = "pack.png"

        author("Apollo")
        author("DawnKiro")
    }

    common {
        mixins.from(file("src/common/main/trimmable_tools.mixins.json"))
    }

    fabric("fabric:1.21.11") {

        loaderVersion = "0.18.2"
        minecraftVersion = "1.21.11"

        dependencies {
            fabricApi("0.141.1")
        }

        includedClient()
        runs {
            client()
            server()
        }

        metadata {
            entrypoint("main") {
                value = "dev.worldgen.trimmable.tools.fabric.TrimmableToolsFabric"
            }
            entrypoint("client") {
                value = "dev.worldgen.trimmable.tools.fabric.TrimmableToolsFabric"
            }
        }
    }

    /*neoforge("neoforge:1.21.11") {
        //dependsOn(shared1217)

        loaderVersion = "21.11.3-beta"
        minecraftVersion = "1.21.11"

        dependencies {
        }

        runs {
            client()
            server()
        }
    }*/
}