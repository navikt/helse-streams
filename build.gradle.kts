import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.RecordingCopyTask
import org.jetbrains.dokka.gradle.DokkaTask

val kafkaVersion = "2.0.1"
val confluentVersion = "5.0.0"
val orgJsonVersion = "20180813"
val ktorVersion = "1.0.0"
val prometheusVersion = "0.5.0"

val junitJupiterVersion = "5.3.1"
val spekVersion = "1.2.1"
val kluentVersion = "1.41"

group = "no.nav.helse"
version = 5

plugins {
   kotlin("jvm") version "1.3.10"
   `java-library`
   `maven-publish`
   signing
   id("com.jfrog.bintray") version "1.8.4"
   id("com.github.johnrengelman.shadow") version "4.0.3"
   id("org.jetbrains.dokka") version "0.9.17"
}

buildscript {
   dependencies {
      classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
   }
}

dependencies {
   compile(kotlin("stdlib"))

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

tasks.withType<Test> {
   useJUnitPlatform()
   testLogging {
      events("passed", "skipped", "failed")
   }
}

tasks.withType<Wrapper> {
   gradleVersion = "4.10.2"
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

         pom {
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
}

ext["signing.gnupg.keyName"] = System.getenv("GPG_KEY_NAME")
ext["signing.gnupg.passphrase"] = System.getenv("GPG_PASSPHRASE")
ext["signing.gnupg.useLegacyGpg"] = true

signing {
   useGpgCmd()
   sign(publishing.publications["mavenJava"])
}

bintray {
   user = System.getenv("BINTRAY_USER")
   key = System.getenv("BINTRAY_KEY")

   setPublications("mavenJava")

   publish = true
   override = true

   filesSpec(delegateClosureOf<RecordingCopyTask> {
      from("${buildDir}/libs") {
         include("*.jar.asc")
      }
      from("${buildDir}/publications/mavenJava") {
         include("pom-default.xml.asc")
         rename("pom-default.xml.asc", "${project.name}-${project.version}.pom.asc")
      }
      into("${(project.group as String).replace(".", "/")}/${project.name}/${project.version}")
   })

   with(pkg){
      repo = "maven"
      name = "${project.group}.${project.name}"
      userOrg = "navikt"
      setLicenses("MIT")
      vcsUrl = "https://github.com/navikt/helse-streams.git"
      desc = "Stream service library for Helse"

      with(version) {
         name = "${project.version}"
      }
   }
}
