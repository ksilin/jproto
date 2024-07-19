// import com.google.protobuf.gradle.id

plugins {
    java
    //id("io.quarkus")
    id("com.google.protobuf") version "0.9.4"
    id("maven-publish")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://packages.confluent.io/maven/")
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val protoVersion = "3.25.3"
val kafkaVersion = "3.7.1"
val confluentVersion = "7.6.1"

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")

    implementation("io.confluent:kafka-streams-protobuf-serde:${confluentVersion}")
    implementation("org.apache.kafka:kafka-clients:${kafkaVersion}")
    implementation("io.confluent:kafka-protobuf-serializer:${confluentVersion}")
    implementation("com.google.protobuf:protobuf-java:${protoVersion}")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("io.rest-assured:rest-assured")
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

configurations.create("native-testCompileOnly")
configurations.create("native-test CompileOnly")

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${protoVersion}"
    }

//    generateProtoTasks {
//        all().forEach { task ->
//            task.plugins {
//                id("java"){
//                    //option("lite")
//                }
//            }
//        }
//    }
    }

sourceSets {
    main {
        proto {
            srcDir("src/main/proto")
        }
        java {
            srcDirs("build/generated/sources/proto/main/java")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.withType<JavaCompile> {
    //dependsOn("generateProto")
    options.isWarnings = true
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
