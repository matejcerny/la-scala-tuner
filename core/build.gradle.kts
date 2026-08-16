plugins {
    scala
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<ScalaCompile>().configureEach {
    scalaCompileOptions.additionalParameters = listOf("-release", "17")
}

dependencies {
    api("org.scala-lang:scala3-library_3:3.8.4")
    api("com.typesafe:config:1.4.3")

    testImplementation("org.scalatest:scalatest_3:3.2.18")
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.scalatestplus:junit-4-13_3:3.2.18.0")
}
