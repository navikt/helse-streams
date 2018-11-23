val kafkaVersion = "2.0.1"
val confluentVersion = "5.0.0"
val orgJsonVersion = "20180813"
val ktorVersion = "1.0.0"
val prometheusVersion = "0.5.0"

val junitJupiterVersion = "5.3.1"
val spekVersion = "1.2.1"
val kluentVersion = "1.41"

version = 2

plugins {
   kotlin("jvm") version "1.3.10"
   `java-library`
   `maven-publish`
}

buildscript {
   dependencies {
      classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
   }
}

dependencies {
   compile(kotlin("stdlib"))

   api("org.apache.kafka:kafka-streams:$kafkaVersion")
   api("io.confluent:kafka-streams-avro-serde:$confluentVersion")
   api("org.json:json:$orgJsonVersion")
   api("io.ktor:ktor-server-netty:$ktorVersion")
   api("io.prometheus:simpleclient_common:$prometheusVersion")
   api("io.prometheus:simpleclient_hotspot:$prometheusVersion")

   testCompile("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
   testCompile("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
   testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
   testCompile("org.amshove.kluent:kluent:$kluentVersion")
   testCompile("org.jetbrains.spek:spek-api:$spekVersion") {
      exclude(group = "org.jetbrains.kotlin")
   }
   testRuntime("org.jetbrains.spek:spek-junit-platform-engine:$spekVersion") {
      exclude(group = "org.junit.platform")
      exclude(group = "org.jetbrains.kotlin")
   }
}

repositories {
   jcenter()
   mavenCentral()
   maven("http://packages.confluent.io/maven/")
   maven("http://packages.confluent.io/maven/")
   maven("https://dl.bintray.com/kotlin/ktor")
}

java {
   sourceCompatibility = JavaVersion.VERSION_11
   targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<Test> {
   useJUnitPlatform()
   testLogging {
      events("passed", "skipped", "failed")
   }
}

tasks.withType<Wrapper> {
   gradleVersion = "4.10.2"
}

val sourcesJar by tasks.registering(Jar::class) {
   classifier = "sources"
   from(sourceSets["main"].allSource)
}

publishing {
   repositories {
      maven {
         // TODO: point to repo on the internets
         group = "no.nav.helse"
         url = uri("$buildDir/repo")
      }
   }
   publications {
      register("mavenJava", MavenPublication::class) {
         from(components["java"])
         artifact(sourcesJar.get())
      }
   }
}



