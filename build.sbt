import Dependencies._

ThisBuild / organization := "cz.cvut.fel.omo"
ThisBuild / scalaVersion := "2.13.7"

lazy val `foodchain` =
  project
    .in(file("."))
    .settings(name := "foodchain")
    .settings(commonSettings)
    .settings(dependencies)

lazy val commonSettings =
  compilerPlugins ++ commonScalacOptions ++ Seq(
    update / evictionWarningOptions := EvictionWarningOptions.empty
  )

lazy val compilerPlugins = Seq(
  addCompilerPlugin(com.olegpy.`better-monadic-for`),
  addCompilerPlugin(org.augustjune.`context-applied`),
  addCompilerPlugin(org.typelevel.`kind-projector`),
)

lazy val commonScalacOptions = Seq(
  Compile / console / scalacOptions := {
    (Compile / console / scalacOptions)
      .value
      .filterNot(_.contains("wartremover"))
      .filterNot(Scalac.Lint.toSet)
      .filterNot(Scalac.FatalWarnings.toSet) :+ "-Wconf:any:silent"
  },
  Test / console / scalacOptions :=
    (Compile / console / scalacOptions).value,
)

lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    // main dependencies
  ),
  libraryDependencies ++= Seq(
    com.github.alexarchambault.`scalacheck-shapeless_1.15`,
    org.scalacheck.scalacheck,
    org.scalatest.scalatest,
    org.scalatestplus.`scalacheck-1-15`,
    org.typelevel.`discipline-scalatest`,
  ).map(_ % Test),
)
