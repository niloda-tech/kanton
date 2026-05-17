plugins {
    kotlin("jvm")
    `maven-publish`
    id("com.gradleup.shadow") version "9.4.0"
    application
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

tasks.test { useJUnitPlatform() }

application {
    mainClass.set("kanton.core.cli.executor.ExecutorKt")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

val buildExecutor = tasks.register("buildExecutor") {
    group = "kanton"
    description = "Build the kanton-executor self-executing binary"
    dependsOn("shadowJar")
    val jarFile = layout.buildDirectory.file("libs/kanton-core-${version}-all.jar")
    val binDir = File(System.getProperty("user.home"), ".kanton/bin")
    inputs.file(jarFile)
    outputs.file(File(binDir, "kanton-executor"))
    doLast {
        binDir.mkdirs()
        val jar = jarFile.get().asFile
        val outputFile = File(binDir, "kanton-executor")
        val tmpFile = File(binDir, ".kanton-executor.tmp")
        val stub = "#!/bin/sh\nexec java -jar \"\$0\" \"\$@\"\n"
        tmpFile.outputStream().use { out ->
            out.write(stub.toByteArray(Charsets.UTF_8))
            out.write(jar.readBytes())
        }
        tmpFile.setExecutable(true)
        tmpFile.renameTo(outputFile)
        println("Executor: ${outputFile.absolutePath}")
    }
}

tasks.register<JavaExec>("bootstrap") {
    group = "kanton"
    description = "Compile kanton scripts into ~/.kanton/bin/"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("kanton.core.cli.compile.BootstrapKt")
    workingDir = rootProject.projectDir
    dependsOn("buildExecutor", "publishToMavenLocal")
}
