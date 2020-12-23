import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
    application
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

allprojects {
    group = "pl.alkhalili.tinkt"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
        jcenter()
    }
}

val kotlinVersion = "1.4.20"
val vertxVersion = "4.0.0"
val junitJupiterVersion = "5.6.0"
val exposedVersion = "0.17.7"
val flywayVersion = "7.3.2"

subprojects {
    apply {
        plugin("kotlin")
        plugin("application")
        plugin("com.github.johnrengelman.shadow")
    }

    val launcherClassName = "io.vertx.core.Launcher"

    application {
        mainClassName = launcherClassName
    }

    dependencies {
        implementation("io.vertx:vertx-core:$vertxVersion")
        implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
        implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
        implementation("io.vertx:vertx-web:$vertxVersion")
        implementation("io.vertx:vertx-web-client:$vertxVersion")
        implementation("org.jetbrains.exposed:exposed:$exposedVersion")
        implementation("com.h2database:h2:1.4.200")
        implementation("ch.qos.logback:logback-classic:1.2.3")
        implementation("org.flywaydb:flyway-core:${flywayVersion}")
        implementation("com.zaxxer:HikariCP:3.4.5")
        implementation("com.google.code.gson:gson:2.8.6")
        implementation(kotlin("stdlib-jdk8"))
        testImplementation("io.vertx:vertx-junit5:$vertxVersion")
        testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    }

    val compileKotlin: KotlinCompile by tasks
    compileKotlin.kotlinOptions.jvmTarget = "11"

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events = setOf(PASSED, SKIPPED, FAILED)
        }
    }
}
