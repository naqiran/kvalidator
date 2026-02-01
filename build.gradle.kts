plugins {
    `java-library`
    alias(libs.plugins.kotlin)
    alias(libs.plugins.nebula)
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(platform(libs.kotest.bom))
    testImplementation(libs.bundles.kotest)
}

kotlin {
    jvmToolchain(25)
}

spotless {
    kotlin {
        ktfmt()
        ktlint()
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.named("check").configure {
    dependsOn("spotlessApply")
}
