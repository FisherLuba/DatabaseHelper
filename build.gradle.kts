import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version "1.9.23"
    `maven-publish`
}

group = "com.fisherl"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
        this.showCauses = true
        this.events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

kotlin {
    jvmToolchain(21)
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "21"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.fisherl"
            artifactId = "databasehelper"
            version = "0.0.1"
            from(components["java"])
        }
    }
}