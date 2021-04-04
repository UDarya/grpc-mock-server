import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

val junitVersion = "4.13.2"
val protocVersion = "3.15.5"
val protoBufVersion = "3.15.5"
val kotlinScriptVersion = "1.4.21"
val kotlinReflectVersion = "1.4.21"
val grpcVersion = "1.28.1"
val grpcKotlinVersion = "0.1.5"
val kotestVersion = "4.4.3"

plugins {
    id("com.google.protobuf") version "0.8.10"
}

dependencies {
    api("javax.annotation:javax.annotation-api:1.3.2")
    api("io.grpc:grpc-kotlin-stub:1.0.0")
    api("io.grpc:grpc-protobuf:$grpcVersion")
    api("io.grpc:grpc-stub:$grpcVersion")

    implementation("com.google.protobuf:protobuf-java:$protoBufVersion")
    implementation("com.google.protobuf:protobuf-java-util:$protoBufVersion")

    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinReflectVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

    implementation("com.squareup:kotlinpoet:1.7.2")

    testImplementation("junit:junit:$junitVersion")
    testImplementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    testImplementation("io.grpc:grpc-services:$grpcVersion")

    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinScriptVersion")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:$kotlinScriptVersion")
    implementation("org.jetbrains.kotlin:kotlin-script-util:$kotlinScriptVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223:$kotlinScriptVersion")
    implementation("net.java.dev.jna:jna:5.7.0")

    implementation("io.grpc:grpc-netty:1.28.1")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")

    testImplementation("junit:junit:4.11")
}

sourceSets {
    main {
        java {
            srcDirs(
                "${protobuf.protobuf.generatedFilesBaseDir}/main/java",
                "${protobuf.protobuf.generatedFilesBaseDir}/main/grpckt",
                "${protobuf.protobuf.generatedFilesBaseDir}/main/grpc"
            )
        }
        proto {
            srcDir(
                "src/main/kotlin/github/udarya/mockserver/proto"
            )
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protocVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.28.1"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:0.1.1"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}
