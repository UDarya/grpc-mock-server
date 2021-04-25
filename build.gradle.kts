plugins {
    kotlin("jvm") version "1.4.0"
    jacoco
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version ("1.4.20")
}

allprojects {
    apply(plugin = "jacoco")

    group = "io.github.udarya"
    version = "0.2"

    repositories {
        mavenCentral()
    }
}
val notToPublish = listOf("test")

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    val sourceSets = the<SourceSetContainer>()

    if (project.name !in notToPublish) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")

        val sourcesJar = task<Jar>("sourcesJar") {
            from(sourceSets["main"].allSource)
            archiveClassifier.set("sources")
        }

        val dokkaJar = task<Jar>("dokkaJar") {
            group = JavaBasePlugin.DOCUMENTATION_GROUP
            archiveClassifier.set("javadoc")
        }

        publishing {
            publications {
                create<MavenPublication>(project.name) {
                    from(components["java"])
                    artifacts {
                        artifact(sourcesJar)
                        artifact(dokkaJar)
                    }

                    pom {
                        name.set("Kotlin grpc mock server")
                        description.set("Kotlin grpc mock server")
                        url.set("https://github.com/UDarya/grpc-mock-server")

                        organization {
                            name.set("io.github.udarya")
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
                            connection.set("scm:git:git://io.github.com/UDarya/grpc-mock-server")
                            developerConnection.set("scm:git:ssh://git@io.github.com:UDarya/grpc-mock-server.git")
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
                            val nexusUsername: String by project
                            val nexusPassword: String by project
                            username = nexusUsername
                            password = nexusPassword
                        }

                        val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                        val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                        url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                    }
                }
            }
        }

        signing {
            sign(publishing.publications[project.name])
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
