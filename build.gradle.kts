import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
   `java-library`
   signing
   `maven-publish`
   kotlin("multiplatform") version "1.9.21"
}

group = "io.kotest.extensions"
version = Ci.version

repositories {
   mavenCentral()
   maven {
      url = uri("https://oss.sonatype.org/content/repositories/snapshots")
   }
}

kotlin {
   jvm {
      withJava()
   }

   if (!project.hasProperty(Ci.JVM_ONLY)) {
      js(IR) {
         browser()
         nodejs()
      }
   }

   sourceSets {

      val commonMain by getting {
         dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
         }
      }

      val jvmMain by getting {
         dependencies {
            implementation(libs.ktor.client.apache)
         }
      }

      val jvmTest by getting {
         dependencies {
            implementation(kotlin("reflect"))
            implementation(libs.kotest.assertions)
            implementation(libs.kotest.runner)
            implementation(libs.mockserver.netty)
            implementation(libs.kotest.extensions.mockserver)
         }
      }

      if (!project.hasProperty(Ci.JVM_ONLY)) {
         val jsMain by getting {
            dependencies {
               implementation(libs.ktor.client.js)
            }
         }
      }
   }
}

tasks.withType<KotlinCompile> {
   kotlinOptions.jvmTarget = "11"
}

tasks.named<Test>("jvmTest").configure {
   useJUnitPlatform()
   testLogging {
      showExceptions = true
      showStandardStreams = true
      exceptionFormat = TestExceptionFormat.FULL
   }
}

val signingKey: String? by project
val signingPassword: String? by project

val publications: PublicationContainer = (extensions.getByName("publishing") as PublishingExtension).publications

signing {
   useGpgCmd()
   if (signingKey != null && signingPassword != null) {
      @Suppress("UnstableApiUsage")
      useInMemoryPgpKeys(signingKey, signingPassword)
   }
   if (Ci.isRelease) {
      sign(publications)
   }
}

java {
   withJavadocJar()
   withSourcesJar()
   toolchain {
      languageVersion.set(JavaLanguageVersion.of(11))
   }
}

publishing {
   repositories {
      maven {
         val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
         val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
         name = "deploy"
         url = if (Ci.isRelease) releasesRepoUrl else snapshotsRepoUrl
         credentials {
            username = System.getenv("OSSRH_USERNAME") ?: ""
            password = System.getenv("OSSRH_PASSWORD") ?: ""
         }
      }
   }

   publications {
      register("mavenJava", MavenPublication::class) {
         from(components["java"])
         pom {
            name.set("kotest-extensions-http")
            description.set("Kotest extension for using HTTP requests while testing")
            url.set("https://www.github.com/kotest/kotest-extensions-http")

            scm {
               connection.set("scm:git:http://www.github.com/kotest/kotest-extensions-http")
               developerConnection.set("scm:git:http://github.com/sksamuel")
               url.set("https://www.github.com/kotest/kotest-extensions-http")
            }

            licenses {
               license {
                  name.set("The Apache 2.0 License")
                  url.set("https://opensource.org/licenses/Apache-2.0")
               }
            }

            developers {
               developer {
                  id.set("sksamuel")
                  name.set("Stephen Samuel")
                  email.set("sam@sksamuel.com")
               }
            }
         }
      }
   }
}
