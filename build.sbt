name := "meal"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions ++= Seq(
  "-encoding", "utf8",
  "-Xfatal-warnings",
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps",
  "-Ypartial-unification"
)

val http4sVersion = "0.20.0"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.0")

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "com.beachape" %% "enumeratum" % "1.5.13",
  "org.jsoup" % "jsoup" % "1.11.3",
  "org.scala-lang.modules" %% "scala-xml" % "1.1.1",
  "io.github.howardjohn" %% "http4s-lambda" % "0.3.1"
)

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.5.3"
).map(_ % "test")

testFrameworks += new TestFramework("utest.runner.Framework")
