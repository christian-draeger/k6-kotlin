@file:Suppress("LocalVariableName")

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
    id("org.jetbrains.dokka")
    id("com.github.ben-manes.versions")
    id("com.adarshr.test-logger")
    jacoco
}

group = "io.github.christian-draeger"
val release_version: String by project
version = release_version

repositories {
    mavenCentral()
}
dependencies {
    val testContainersVersion = "1.16.0"
    implementation("org.testcontainers:testcontainers:$testContainersVersion")
    implementation("org.testcontainers:influxdb:$testContainersVersion")
    implementation("ch.qos.logback:logback-classic:1.2.5")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    withType<JacocoReport> {
        reports {
            xml.isEnabled = true
        }
    }
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events(
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.FAILED
            )
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
    }
    withJavadocJar()
    withSourcesJar()
}

testlogger {
    setTheme("mocha-parallel")
    slowThreshold = 1000
}

jacoco {
    toolVersion = "0.8.6"
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = rootProject.name
            from(components["java"])
            pom {
                name.set("k6-kotlin")
                description.set("run k6 load and performance testing scenarios from your JVM project on an out-of-the box but configurable k6 infra spin-up by the use of Docker.")
                url.set("https://github.com/christian-draeger/${rootProject.name}")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("christian-draeger")
                        name.set("Christian Dräger")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/christian-draeger/${rootProject.name}.git")
                    developerConnection.set("scm:git:ssh://github.com:christian-draeger/${rootProject.name}.git")
                    url.set("https://github.com/christian-draeger/${rootProject.name}/tree/main")
                }
            }
        }
    }
    signing {
        sign(publishing.publications["mavenJava"])
        val signingKeyId: String? by project
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    }
}
