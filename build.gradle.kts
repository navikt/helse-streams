import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kafkaVersion = "2.0.1"
val confluentVersion = "5.0.0"
val orgJsonVersion = "20180813"
val ktorVersion = "1.0.0"
val prometheusVersion = "0.5.0"

val junitJupiterVersion = "5.3.1"
val spekVersion = "1.2.1"
val kluentVersion = "1.41"

group = "no.nav.helse"
version = 15

plugins {
   kotlin("jvm") version "1.3.11"
   `java-library`
   `maven-publish`
   signing
   id("com.github.johnrengelman.shadow") version "4.0.3"
   id("org.jetbrains.dokka") version "0.9.17"
   id("io.codearte.nexus-staging") version "0.12.0"
}

buildscript {
   dependencies {
      classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
   }
}

dependencies {
   implementation(kotlin("stdlib"))

   compile("org.apache.kafka:kafka-streams:$kafkaVersion")
   compile("io.confluent:kafka-streams-avro-serde:$confluentVersion")
   compile("org.json:json:$orgJsonVersion")
   compile("io.ktor:ktor-server-netty:$ktorVersion")
   compile("io.prometheus:simpleclient_common:$prometheusVersion")
   compile("io.prometheus:simpleclient_hotspot:$prometheusVersion")

   compile("ch.qos.logback:logback-classic:1.2.3")
   compile("net.logstash.logback:logstash-logback-encoder:5.2")

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
   maven("https://dl.bintray.com/kotlin/ktor")
}

java {
   sourceCompatibility = JavaVersion.VERSION_11
   targetCompatibility = JavaVersion.VERSION_11
}

tasks.named<KotlinCompile>("compileKotlin") {
   kotlinOptions.jvmTarget = "1.8"
}

tasks.named<KotlinCompile>("compileTestKotlin") {
   kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
   useJUnitPlatform()
   testLogging {
      events("passed", "skipped", "failed")
   }
}

tasks.withType<Wrapper> {
   gradleVersion = "5.0"
}

val dokka = tasks.withType<DokkaTask> {
   outputFormat = "html"
   outputDirectory = "$buildDir/javadoc"
}

val sourcesJar by tasks.registering(Jar::class) {
   classifier = "sources"
   from(sourceSets["main"].allSource)
}

val javadocJar by tasks.registering(Jar::class) {
   dependsOn(dokka)
   classifier = "javadoc"
   from(buildDir.resolve("javadoc"))
}

artifacts {
   add("archives", sourcesJar)
   add("archives", javadocJar)
}

publishing {
   publications {
      register("mavenJava", MavenPublication::class) {
         from(components["java"])
         artifact(sourcesJar.get())
         artifact(javadocJar.get())

         artifactId = "streams"

         pom {
            name.set("streams")
            description.set("Stream service library for Helse")
            url.set("https://github.com/navikt/helse-streams")
            withXml {
               asNode().appendNode("packaging", "jar")
            }
            licenses {
               license {
                  name.set("MIT License")
                  url.set("https://opensource.org/licenses/MIT")
                  distribution.set("repo")
               }
            }
            developers {
               developer {
                  organization.set("NAV (Arbeids- og velferdsdirektoratet) - The Norwegian Labour and Welfare Administration")
                  organizationUrl.set("https://www.nav.no")
               }
            }
            scm {
               connection.set("scm:git:https://github.com/navikt/helse-streams.git")
               developerConnection.set("scm:git:https://github.com/navikt/helse-streams.git")
               url.set("https://github.com/navikt/helse-streams.git")
            }
         }
      }
   }

   repositories {
      maven {
         credentials {
            username = System.getenv("OSSRH_JIRA_USERNAME")
            password = System.getenv("OSSRH_JIRA_PASSWORD")
         }
         val version = "${project.version}"
         url = if (version.endsWith("-SNAPSHOT")) {
            uri("https://oss.sonatype.org/content/repositories/snapshots")
         } else {
            uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
         }
      }
   }
}

ext["signing.gnupg.keyName"] = System.getenv("GPG_KEY_NAME")
ext["signing.gnupg.passphrase"] = System.getenv("GPG_PASSPHRASE")
ext["signing.gnupg.useLegacyGpg"] = true

signing {
   useGpgCmd()
   sign(publishing.publications["mavenJava"])
}

nexusStaging {
   username = System.getenv("OSSRH_JIRA_USERNAME")
   password = System.getenv("OSSRH_JIRA_PASSWORD")
   packageGroup = "no.nav"
}
