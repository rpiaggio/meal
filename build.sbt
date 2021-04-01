name := "meal"

version := "0.2"

scalaVersion := "2.13.5"

Global / onChangedBuildSource := ReloadOnSourceChanges

scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-Xfatal-warnings",
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)

val http4sVersion = "0.21.19"

addCompilerPlugin(
  "org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full
)

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "com.beachape" %% "enumeratum" % "1.6.1",
  "org.jsoup" % "jsoup" % "1.13.1",
  "org.scala-lang.modules" %% "scala-xml" % "2.0.0-M5",
  "io.github.howardjohn" %% "http4s-lambda" % "0.4.1-SNAPSHOT"
)

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.7.8"
).map(_ % "test")

testFrameworks += new TestFramework("utest.runner.Framework")
