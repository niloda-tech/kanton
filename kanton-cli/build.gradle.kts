plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "9.4.0"
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(project(":kanton-core"))
    implementation("com.github.ajalt.clikt:clikt:5.1.0")
    implementation("org.json:json:20240303")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("kanton.cli.MainKt")
}

val installCli = tasks.register("installCli") {
    group = "kanton"
    description = "Build and install the kanton CLI binary to ~/.kanton/bin/"
    dependsOn("shadowJar")
    val jarFile = layout.buildDirectory.file("libs/kanton-cli-${version}-all.jar")
    val binDir = File(System.getProperty("user.home"), ".kanton/bin")
    inputs.file(jarFile)
    outputs.file(File(binDir, "kanton"))
    doLast {
        binDir.mkdirs()
        val jar = jarFile.get().asFile
        val outputFile = File(binDir, "kanton")
        val tmpFile = File(binDir, ".kanton.tmp")
        val stub = "#!/bin/sh\nexec java -jar \"\$0\" \"\$@\"\n"
        tmpFile.outputStream().use { out ->
            out.write(stub.toByteArray(Charsets.UTF_8))
            out.write(jar.readBytes())
        }
        tmpFile.setExecutable(true)
        tmpFile.renameTo(outputFile)
        println("Installed: ${outputFile.absolutePath}")
    }
}
