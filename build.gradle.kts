val logback_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.12"
    `maven-publish`
}

group = "com.techullurgy"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-client-apache")
}

publishing {
    repositories {
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/Mikkareem/ktor-consul")
            credentials {
                username = System.getenv("GITHUB_PACKAGE_USER")
                password = System.getenv("GITHUB_PACKAGE_CREDENTIALS")
            }
        }
    }
    publications {
        register<MavenPublication>("default") {
            groupId = "com.techullurgy"
            artifactId = "ktor-consul"
            version = "0.0.1-alpha"
            from(components["java"])
        }
    }
}