import Dependencies._

ThisBuild / scalafixScalaBinaryVersion := scalaBinaryVersion.value
ThisBuild / scalafixDependencies ++= Seq(
  com.github.liancheng.`organize-imports`
)

ThisBuild / semanticdbEnabled := true
// ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / semanticdbVersion := "4.4.30"
