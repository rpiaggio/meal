name := "meal"

version := "0.4"

scalaVersion := "3.10.0"

Global / onChangedBuildSource := ReloadOnSourceChanges

scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-Werror",
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)

val http4sVersion = "0.23.38"
val http4sBlazeVersion = "0.23.18"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sBlazeVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sBlazeVersion,
  "org.jsoup" % "jsoup" % "1.23.2",
  "org.scala-lang.modules" %% "scala-xml" % "2.5.0",
  "org.typelevel" %% "feral-lambda-http4s" % "0.3.1"
)

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.9.5"
).map(_ % "test")

testFrameworks += new TestFramework("utest.runner.Framework")

// Fixed name, so serverless.yml doesn't need updating when the version changes.
assembly / assemblyJarName := "meal.jar"

// Tests against the real sites (`*LiveTest`) only run with `sbt Live/test`, since they depend on the network
// and on the sites not changing.
lazy val Live = config("live").extend(Test)

lazy val root = project
  .in(file("."))
  .configs(Live)
  .settings(
    inConfig(Live)(Defaults.testTasks),
    Test / testOptions += Tests.Filter(!_.endsWith("LiveTest")),
    Live / testOptions := Seq(Tests.Filter(_.endsWith("LiveTest")))
  )
