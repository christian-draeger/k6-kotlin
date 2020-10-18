import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    application
}
group = "io.github.christian-draeger"
version = "0.1.0"

repositories {
    mavenCentral()
}
dependencies {
    val testContainersVersion = "1.15.0-rc2"
    implementation("org.testcontainers:testcontainers:$testContainersVersion")
    implementation("org.testcontainers:influxdb:$testContainersVersion")

    // implementation("io.apisense.embed.influx:embed-influxDB:1.2.1")

    implementation("ch.qos.logback:logback-classic:1.1.7")
    testImplementation(kotlin("test-junit5"))
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
application {
    mainClassName = "MainKt"
}