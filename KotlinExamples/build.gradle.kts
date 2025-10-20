plugins {
    kotlin("jvm") version "2.2.10"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2") // added for runTest & TestScope APIs
    testImplementation("app.cash.turbine:turbine:1.2.1") // flow testing helper
}

tasks.test {
    useJUnitPlatform()
}
