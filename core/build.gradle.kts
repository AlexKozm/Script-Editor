import org.jetbrains.kotlin.gradle.tasks.KotlinTest

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotest)
    alias(libs.plugins.ksp)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
//            implementation(libs.kotlin.test)
            implementation(libs.kotest)
            implementation(libs.kotest.assertions)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutinesSwing)
        }
        jvmTest.dependencies {
            implementation(libs.kotest.junit)
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    logger.lifecycle("UP-TO-DATE check for $name is disabled, forcing it to run.")
    outputs.upToDateWhen { false }
}

tasks.withType<KotlinTest>().configureEach {
    failOnNoDiscoveredTests = false
}