
plugins {
    kotlin("jvm") version "1.4.31"
   // id("io.gitlab.arturbosch.detekt").version("1.16.0")
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

val ktor_version = "1.5.1"
val nee_version = "0.7.3"

dependencies {
    implementation("pl.setblack:nee-ctx-web-ktor:$nee_version")
    //detektPlugins("pl.setblack:kure-potlin:0.5.0")
   // detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.16.0")

    implementation("io.ktor:ktor-http-jvm:$ktor_version")
    implementation("io.ktor:ktor-jackson:$ktor_version")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.0")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.12.0")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.4.3")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.4.3")
    testImplementation("pl.setblack:nee-ctx-web-test:$nee_version")
}

tasks.withType<Test> {
    useJUnitPlatform()
}


val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions.apply {
    jvmTarget = "1.8"
    javaParameters = true
    allWarningsAsErrors = false
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}

val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileTestKotlin.kotlinOptions.apply {
    jvmTarget = "1.8"
    javaParameters = true
    allWarningsAsErrors = false
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}

//tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
//    this.jvmTarget = "1.8"
//    this.classpath.setFrom(compileKotlin.classpath.asPath)
//}

//tasks {
//    "build" {
//        dependsOn("detektMain")
//    }
//}


val fatJar = task("fatJar", type = Jar::class) {
    archiveBaseName.set("server-all")
    manifest {
        attributes["Implementation-Title"] = "Damager Game"
        attributes["Main-Class"] = "damager.web.ServerKt"
    }
    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}
