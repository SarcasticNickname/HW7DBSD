plugins {
    kotlin("jvm") version "2.0.21"
    id("org.flywaydb.flyway") version "11.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    implementation("org.jetbrains.exposed:exposed-core:0.43.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.43.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.43.0")
    implementation("org.flywaydb:flyway-core:11.0.0")
    implementation("org.flywaydb:flyway-database-postgresql:11.0.0")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("com.github.javafaker:javafaker:1.0.2")
    implementation("org.jetbrains.exposed:exposed-java-time:0.43.0")
    testImplementation(kotlin("test"))
}

// Установка одинаковой версии JVM для Kotlin и Java
kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.test {
    useJUnitPlatform()
}