ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "ReverseProzy"
  )

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % "0.23.30",
  "org.http4s" %% "http4s-ember-server" % "0.23.30",  // Using ember-server instead of blaze
  "org.http4s" %% "http4s-ember-client" % "0.23.30",  // Using ember-client instead of blaze

  // Play JSON
  "com.typesafe.play" %% "play-json" % "2.10.6",

  // Cats effect for functional effects
  "org.typelevel" %% "cats-effect" % "3.5.7",
"com.lihaoyi" %% "ammonite" % "2.5.8"
)

libraryDependencies += "com.lihaoyi" %% "ammonite" % "3.0.0-2-6342755f"




resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)