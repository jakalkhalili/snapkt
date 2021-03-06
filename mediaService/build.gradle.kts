dependencies {
    compile(project(":common"))

}

val mainVerticleName = "pl.alkhalili.snapkt.media.MainVerticle"
val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"
val launcherClassName = "io.vertx.core.Launcher"

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveClassifier.set("fat")
    manifest {
        attributes(mapOf("Main-Verticle" to mainVerticleName))
    }
    mergeServiceFiles {
        include("META-INF/services/io.vertx.core.spi.VerticleFactory")
    }
}

tasks.withType<JavaExec> {
    args = listOf(
        "run",
        mainVerticleName,
        "--launcher-class=$launcherClassName"
    )
}

jib {
    from {
        image = "gcr.io/distroless/java:11"
    }
    to {
        image = "snapkt/media-service"
        tags = setOf("v1")
    }
    container {
        mainClass = "io.vertx.core.Launcher"
        args = listOf("run", mainVerticleName, "--launcher-class=$launcherClassName")
        ports = listOf("8080")
    }
}
