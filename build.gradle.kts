import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
}

group = "io.github.christian-draeger"
version = "0.1.0"

repositories {
    mavenCentral()
}
dependencies {
    val testContainersVersion = "1.16.0"
    implementation("org.testcontainers:testcontainers:$testContainersVersion")
    implementation("org.testcontainers:influxdb:$testContainersVersion")
    implementation("ch.qos.logback:logback-classic:1.1.7")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    test {
        useJUnitPlatform()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
