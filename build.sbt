val scala3Version = "3.5.0"
val projectName   = "echo-grpc-server"
val scmOwner      = "bcarter97"

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / scalafmtOnCompile    := true

ThisBuild / organization    := s"io.github.$scmOwner"
ThisBuild / dynverSeparator := "-"
ThisBuild / usePipelining   := true

ThisBuild / versionScheme := Some("early-semver")
ThisBuild / licenses      := List("BSD New" -> url("https://opensource.org/licenses/BSD-3-Clause"))
ThisBuild / homepage      := Some(url(s"https://github.com/$scmOwner/$projectName"))

lazy val root = project
  .in(file("."))
  .settings(
    name         := projectName,
    scalaVersion := scala3Version,
    description  := "gRPC server that response with a status after the given delay"
  )
  .settings(
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version)
  )
  .settings(
    libraryDependencies ++= Seq(
      "com.github.pureconfig" %% "pureconfig-cats-effect"    % "0.17.7",
      "com.github.pureconfig" %% "pureconfig-generic-scala3" % "0.17.7",
      "com.github.pureconfig" %% "pureconfig-ip4s"           % "0.17.7",
      "com.thesamet.scalapb"  %% "scalapb-runtime"           % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "io.circe"              %% "circe-generic"             % "0.14.9",
      "io.circe"              %% "circe-parser"              % "0.14.9",
      "io.grpc"                % "grpc-netty-shaded"         % scalapb.compiler.Version.grpcJavaVersion,
      "io.grpc"                % "grpc-services"             % scalapb.compiler.Version.grpcJavaVersion,
      "org.typelevel"         %% "cats-effect"               % "3.5.4"
    )
  )
  .settings(
    run / fork := true
  )
  .settings(
    dockerRepository     := Some("ghcr.io"),
    dockerBaseImage      := "eclipse-temurin:21-jre-jammy",
    Docker / packageName := s"$scmOwner/$projectName",
    dockerUpdateLatest   := true
  )
  .settings(SbtTpolecat.options)
  .enablePlugins(Fs2Grpc, BuildInfoPlugin, DockerPlugin, JavaAppPackaging)
