buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin", "kotlin-gradle-plugin", Versions.kotlin)
        classpath("org.jetbrains.kotlin", "kotlin-allopen", Versions.kotlin)
        classpath("org.springframework.boot", "spring-boot-gradle-plugin", Versions.springboot)
        classpath("org.jlleitschuh.gradle", "ktlint-gradle", Versions.ktlintPlugin)
        classpath("com.google.protobuf", "protobuf-gradle-plugin", Versions.protobufPlugin)
    }
}

plugins {
    `java-library`
    idea
    jacoco
}

subprojects {

    apply {
        plugin("kotlin")
        plugin("idea")
        plugin("java-library")
        plugin("jacoco")
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    group = "com.figure.wallet"
    version = "1.0-SNAPSHOT"

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

        fun figureNexusUsername() = findProperty("nexusUser")?.toString() ?: System.getenv("NEXUS_USER")
        fun figureNexusPassword() = findProperty("nexusPass")?.toString() ?: System.getenv("NEXUS_PASS")

        maven {
            url = uri("https://nexus.figure.com/repository/mirror")
            credentials {
                username = figureNexusUsername()
                password = figureNexusPassword()
            }
        }
        maven {
            url = uri("https://nexus.figure.com/repository/figure")
            credentials {
                username = figureNexusUsername()
                password = figureNexusPassword()
            }
        }
        maven { url = uri("https://repo.spring.io/plugins-release") }
    }

    dependencies {
        implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", Versions.kotlin)
        implementation("org.jetbrains.kotlin", "kotlin-stdlib", Versions.kotlin)
        implementation("org.jetbrains.kotlin", "kotlin-reflect", Versions.kotlin)
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)

        reports {
            xml.isEnabled = false
            csv.isEnabled = false
            html.isEnabled = true
        }
    }
}
