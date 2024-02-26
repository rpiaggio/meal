name := "meal"

version := "0.2"

scalaVersion := "2.13.6"

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

val http4sVersion = "0.23.4"

addCompilerPlugin(
  "org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full
)

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "com.beachape" %% "enumeratum" % "1.7.0",
  "org.jsoup" % "jsoup" % "1.14.3",
  "org.scala-lang.modules" %% "scala-xml" % "2.0.1",
  "io.github.howardjohn" %% "http4s-lambda" % "0.4.1-SNAPSHOT"
)

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.7.10"
).map(_ % "test")

testFrameworks += new TestFramework("utest.runner.Framework")
