buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin", "kotlin-gradle-plugin", Versions.kotlin)
        classpath("org.jetbrains.kotlin", "kotlin-allopen", Versions.kotlin)
        classpath("org.jlleitschuh.gradle", "ktlint-gradle", Versions.ktlintPlugin)
        classpath("com.google.protobuf", "protobuf-gradle-plugin", Versions.protobufPlugin)
    }
}

plugins {
    idea
    `java-library`
    `maven-publish`
    signing
}

subprojects {

    apply {
        plugin("kotlin")
        plugin("idea")
        plugin("java-library")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("maven-publish")
        plugin("signing")
    }

    group = "io.provenance.protobuf"
    version = project.property("version")?.takeIf { it != "unspecified" } ?: "1.0-SNAPSHOT"

    project.ext.properties["kotlin_version"] = Versions.kotlin

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenCentral()
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    dependencies {
        implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", Versions.kotlin)
        implementation("org.jetbrains.kotlin", "kotlin-stdlib", Versions.kotlin)
        implementation("org.jetbrains.kotlin", "kotlin-reflect", Versions.kotlin)
    }

    publishing {
        repositories {
            maven {
                name = "MavenCentral"
                url = if (version == "1.0-SNAPSHOT") {
                   uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                } else {
                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                }

                credentials {
                    username = findProject("ossrhUsername")?.toString() ?: System.getenv("OSSRH_USERNAME")
                    password = findProject("ossrhPassword")?.toString() ?: System.getenv("OSSRH_PASSWORD")
                }
            }
        }
        publications {
            create<MavenPublication>("maven") {
                groupId = "io.provenance.protobuf"
                artifactId = "pb-proto-java"

                from(components["java"])

                pom {
                    name.set("Provenance Protobufs in Java")
                    description.set("Compiles Provenance Protobufs into their Java representation.")
                    url.set("https://provenance.io")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("wshin")
                            name.set("Wontaek Shin")
                            email.set("wshin@figure.com")
                        }

                        developer {
                            id.set("gabrinthei")
                            name.set("James Sonntag")
                            email.set("jsonntag@figure.com")
                        }

                        developer {
                            id.set("scirner22")
                            name.set("Stephen Cirner")
                            email.set("scirner@figure.com")
                        }
                    }

                    scm {
                        connection.set("git@github.com:provenance-io/provenance-protobuf-java.git")
                        developerConnection.set("git@github.com:provenance-io/provenance-protobuf-java.git")
                        url.set("https://github.com/provenance-io/provenance-protobuf-java")
                    }
                }
            }
        }

        signing {
            sign(publishing.publications["maven"])
        }

        tasks.javadoc {
            if(JavaVersion.current().isJava9Compatible) {
                (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
            }
        }
    }
}
