plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.ktor)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }

}

ktor {
    fatJar {
        archiveFileName.set("jlberlinserver.jar")
    }
}

dependencies {
    implementation(project(":JLBerlinModel"))

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.sse)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}