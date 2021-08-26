name := """mockws-caffeine-test"""
organization := "com.example"
version := "1.0-SNAPSHOT"

scalaVersion := "2.13.6"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  ws,
  "com.github.blemale" %% "scaffeine" % "5.1.0",
  "org.awaitility" % "awaitility-scala" % "4.1.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.9" % Test,
  "de.leanovate.play-mockws" %% "play-mockws" % "2.8.1" % Test
)

dependencyOverrides ++= Seq(
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1"
)
