addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full)
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")

name := "free-style-example"

version := "0.1"

scalaVersion := "2.12.4"

scalacOptions ++= Seq(
  "-Ypartial-unification",
  "-language:higherKinds"
)

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "io.frees" %% "frees-core" % "0.4.6",
  "io.frees" %% "frees-async" % "0.4.6",
  "com.github.finagle" %% "finch-core" % "0.16.0-M5",
  "com.github.finagle" %% "finch-circe" % "0.16.0-M5",
  "io.circe" %% "circe-generic" % "0.9.0-M2",
  "org.scalatest" % "scalatest_2.12" % "3.0.4" % Test,
)
