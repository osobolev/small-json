import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

description = "Small JSON library"

plugins {
    id("com.vanniktech.maven.publish") version "0.37.0"
    id("module-lib")
}

group = "io.github.osobolev"
version = "1.4"

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("${project.group}", "${project.name}", "${project.version}")
    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = SourcesJar.Sources()
    ))
}

mavenPublishing.pom {
    name = "small-json"
    description = "Small JSON library"
    url = "https://github.com/osobolev/small-json"
    licenses {
        license {
            name = "The Apache License, Version 2.0"
            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
        }
    }
    developers {
        developer {
            name = "Oleg Sobolev"
            organizationUrl = "https://github.com/osobolev"
        }
    }
    scm {
        connection = "scm:git:https://github.com/osobolev/small-json.git"
        developerConnection = "scm:git:https://github.com/osobolev/small-json.git"
        url = "https://github.com/osobolev/small-json"
    }
}

tasks.withType(com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class).configureEach {
    rejectVersionIf {
        candidate.version.contains("-M") ||
        candidate.version.contains("-RC") ||
        candidate.version.contains("-rc")
    }
}
