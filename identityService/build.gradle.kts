dependencies {
    compile(project(":common"))

    // Temporary solution. I would like to avoid using dependencies from Spring framework family.
    implementation("org.springframework.security:spring-security-core:5.4.2")
}

val mainVerticleName = "pl.alkhalili.snapkt.identity.MainVerticle"
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
