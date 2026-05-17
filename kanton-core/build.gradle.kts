plugins {
    kotlin("jvm")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api("com.github.ajalt.clikt:clikt:5.1.0")
    testImplementation(kotlin("test"))
    testImplementation("io.cucumber:cucumber-java:7.22.2")
    testImplementation("io.cucumber:cucumber-java8:7.22.2")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.22.2")
    testImplementation("org.junit.platform:junit-platform-suite:1.11.4")
}

kotlin {
    jvmToolchain(17)
}

tasks.register<JavaExec>("bootstrap") {
    group = "kanton"
    description = "Compile kanton-compile and kanton-scaffold into bin/ if not already present"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("kanton.core.cli.compile.BootstrapKt")
    workingDir = rootProject.projectDir
}
