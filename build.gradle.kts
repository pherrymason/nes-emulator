import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.41"
}

group = "com.pherrymason"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.1")
    compile(kotlin("script-runtime"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.getting(KotlinCompile::class) {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}