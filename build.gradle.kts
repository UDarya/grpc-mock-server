plugins {
    kotlin("jvm") version "1.4.20"
    jacoco
    `maven-publish`
}

allprojects {
    apply(plugin = "jacoco")

    group = "github.udarya.mockserver"
    version = "0.1"

    repositories {
        mavenCentral()
    }
}
val notToPublish = listOf("test")

subprojects {
    apply(plugin = "kotlin")

//    tasks.withType<KotlinCompile> {
//        kotlinOptions.jvmTarget = "11"
//    }

    val sourceSets = the<SourceSetContainer>()

    if (project.name !in notToPublish) {
        apply(plugin = "maven-publish")

        val sourcesJar = task<Jar>("sourcesJar") {
            from(sourceSets["main"].allSource)
            archiveClassifier.set("sources")
        }

        publishing {
            publications {
                create<MavenPublication>(project.name) {
                    from(components["java"])
                    artifacts {
                        artifact(sourcesJar)
                    }

                    pom {
                        name.set("Kotlin grpc mock server")
                        description.set("Kotlin grpc mock server")
                        url.set("https://github.com/UDarya/grpc-mock-server")

                        organization {
                            name.set("github.udarya")
                            url.set("https://github.com/udarya")
                        }
                        licenses {
                            license {
                                name.set("Apache License 2.0")
                                url.set("https://github.com/UDarya/grpc-mock-server/blob/main/LICENSE")
                            }
                        }
                        scm {
                            url.set("https://github.com/UDarya/grpc-mock-server")
                            connection.set("scm:git:git://github.com/UDarya/grpc-mock-server")
                            developerConnection.set("scm:git:ssh://git@github.com:UDarya/grpc-mock-server.git")
                        }
                        developers {
                            developer {
                                name.set("Darya")
                            }
                        }
                    }
                }

                repositories {
                    maven {
                        credentials {
                            val nexusUsername: String? by project
                            val nexusPassword: String? by project
                            username = nexusUsername
                            password = nexusPassword
                        }

                        val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                        val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                        url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                    }
                }
            }
        }
    }

}


tasks.withType<JacocoReport> {
    val containers = subprojects.map { it.the<SourceSetContainer>()["main"] }

    val output = containers.flatMap { it.output }
    val sources = containers.flatMap { it.allSource.srcDirs }

    val exec = subprojects.flatMap { it.tasks }
        .filterIsInstance<Test>()
        .flatMap { files(it) }
        .filter { it.exists() && it.name.endsWith(".exec") }

    additionalSourceDirs.setFrom(sources)
    sourceDirectories.setFrom(sources)
    classDirectories.setFrom(output)
    executionData.setFrom(exec)

    reports {
        xml.isEnabled = true
        xml.destination = File("$buildDir/reports/jacoco/report.xml")
        html.isEnabled = false
    }
}

tasks {
    withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}
