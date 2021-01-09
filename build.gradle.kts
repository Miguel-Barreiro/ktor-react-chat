plugins {
    kotlin("jvm") version "1.4.10"
}

group = "lindar"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

val ktorVersion: String by project
val logbackVersion: String by project

dependencies {
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-websockets:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation( "mysql:mysql-connector-java:8.0.22")
    implementation( "com.google.code.gson:gson:2.8.5")
}