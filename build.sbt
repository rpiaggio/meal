name := "meal"

version := "0.3"

scalaVersion := "3.6.3"

Global / onChangedBuildSource := ReloadOnSourceChanges

scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  // "-Xfatal-warnings",
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)

val http4sVersion = "0.23.30"
val http4sBlazeVersion = "0.23.17"

// addCompilerPlugin(
//   "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
// )

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sBlazeVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sBlazeVersion,
  // "com.beachape" %% "enumeratum" % "1.7.0",
  "org.jsoup" % "jsoup" % "1.18.3",
  "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
  "io.github.howardjohn" %% "http4s-lambda" % "0.4.2-SNAPSHOT"
)

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.8.5"
).map(_ % "test")

testFrameworks += new TestFramework("utest.runner.Framework")
