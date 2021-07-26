import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import java.io.File

plugins {
    id("com.google.protobuf")
    id("de.undercouch.download")
    id("maven-publish")
}

sourceSets {
    main {
        java {
            java {
                srcDir("build/generated/source/proto/main/java")
            }
            proto {
                srcDir("src/main/proto")
            }
        }
    }
}

dependencies {
    // Protobuf
    implementation("com.google.protobuf", "protobuf-java", Versions.protobuf)
    protobuf("com.figure.protobuf", "protobuf-java", Versions.protoUtil) { isTransitive = false }
    api("com.figure.protobuf", "protobuf-java-util", Versions.protoUtil)

    // Grpc
    implementation("io.grpc", "grpc-stub", Versions.grpc)
    implementation("io.grpc", "grpc-protobuf", Versions.grpc) {
        exclude("com.google.protobuf")
    }

    // Validation
    implementation("javax.annotation", "javax.annotation-api", "1.3.2")
}

val downloadCosmosProtos = tasks.create<Download>("downloadCosmosProtos") {
    src("https://github.com/cosmos/cosmos-sdk/tarball/v${Versions.cosmos}")
    dest(File(buildDir, "${Versions.cosmos}.tar.gz"))
    onlyIfModified(true)
}

val downloadAndUntarCosmosProtos = tasks.create<Copy>("downloadAndUntarCosmosProtos") {
    dependsOn(downloadCosmosProtos)
    from(tarTree(downloadCosmosProtos.dest)) {
        include("**/*.proto")
        eachFile {
            val segmentsPath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
            val newRelativePath =
                when {
                    segmentsPath.toString().startsWith("third_party") ->
                        RelativePath(true, *segmentsPath.segments.drop(1).toTypedArray())
                    else -> segmentsPath
                }

            this.relativePath = newRelativePath
        }
        includeEmptyDirs = false
    }
    into("src/main")
}

val downloadProvenanceProtos = tasks.create<Download>("downloadProvenanceProtos") {
    src("https://github.com/provenance-io/provenance/releases/download/v${Versions.pbProto}/protos-v${Versions.pbProto}.zip")
    dest(File(buildDir, "${Versions.pbProto}.tar.gz"))
    onlyIfModified(true)
}

val downloadAndUnzipProvenanceProtos = tasks.create<Copy>("downloadAndUnzipProvenanceProtos") {
    dependsOn(downloadAndUntarCosmosProtos, downloadProvenanceProtos)
    from(zipTree(downloadProvenanceProtos.dest))
    into("src/main")
}

val downloadWasmProtos = tasks.create<Download>("downloadWasmProtos") {
    src("https://github.com/CosmWasm/wasmd/archive/refs/tags/${Versions.wasmd}.tar.gz")
    dest(File(buildDir, "${Versions.wasmd}.tar.gz"))
    onlyIfModified(true)
}

val downloadAndUntarWasmdProtos = tasks.create<Copy>("downloadAndUntarWasmdProtos") {
    dependsOn(downloadWasmProtos)
    from(tarTree(downloadWasmProtos.dest)) {
        include("**/x/wasm/internal/types/*.proto")
        eachFile {
            relativePath = RelativePath(true, "proto", *relativePath.segments.drop(1).toTypedArray())
        }
        includeEmptyDirs = false
    }
    into("src/main")
}

tasks.create("downloadAllExternalProtos") {
    dependsOn(downloadAndUnzipProvenanceProtos, downloadAndUntarCosmosProtos, downloadAndUntarWasmdProtos)
    tasks.findByName("downloadAndUnzipProvenanceProtos")!!.mustRunAfter("downloadAndUntarCosmosProtos")
    tasks.findByName("downloadAndUnzipProvenanceProtos")!!.mustRunAfter("downloadAndUntarWasmdProtos")
}

tasks.compileKotlin {
    dependsOn("generateProto")
}

tasks.test {
    useJUnitPlatform {}
    testLogging {
        events(PASSED, SKIPPED, FAILED)
        exceptionFormat = FULL
    }
}

// Protobufs
protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:${Versions.protobuf}"
    }
    plugins {
        // Optional: an artifact spec for a protoc plugin, with "grpc" as
        // the identifier, which can be referred to in the "plugins"
        // container of the "generateProtoTasks" closure.
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.0.0-pre2"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without options.
                id("grpc")
            }
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://nexus.figure.com/repository/figure")
            credentials {
                username = findProperty("nexusUser")?.toString() ?: System.getenv("NEXUS_USER")
                password = findProperty("nexusPass")?.toString() ?: System.getenv("NEXUS_PASS")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.provenance.protobuf"
            artifactId = "pb-proto-java"
            version = findProperty("artifactVersion")?.toString() ?: "default-1"
            from(components["java"])
        }
    }
}
