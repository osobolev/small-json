import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

description = "Small JSON library"

plugins {
    id("com.vanniktech.maven.publish") version "0.36.0"
    `module-lib`
}

group = "io.github.osobolev"
version = "1.4"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test").configure {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("${project.group}", "${project.name}", "${project.version}")
    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = true
    ))
}

mavenPublishing.pom {
    name.set("small-json")
    description.set("Small JSON library")
    url.set("https://github.com/osobolev/small-json")
    licenses {
        license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
    developers {
        developer {
            name.set("Oleg Sobolev")
            organizationUrl.set("https://github.com/osobolev")
        }
    }
    scm {
        connection.set("scm:git:https://github.com/osobolev/small-json.git")
        developerConnection.set("scm:git:https://github.com/osobolev/small-json.git")
        url.set("https://github.com/osobolev/small-json")
    }
}
