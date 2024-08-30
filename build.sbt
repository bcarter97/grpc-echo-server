val scala3Version = "3.5.0"

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / scalafmtOnCompile    := true

ThisBuild / dynverSeparator := "-"

ThisBuild / versionScheme := Some("early-semver")

lazy val root = project
  .in(file("."))
  .settings(
    name              := "grpc-echo-server",
    scalaVersion      := scala3Version,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    buildInfoKeys     := Seq[BuildInfoKey](name, version),
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
    ),
    run / fork        := true
  )
  .settings(
    dockerRepository     := sys.env.get("DOCKER_REPOSITORY"),
    dockerBaseImage      := "eclipse-temurin:21-jre-jammy",
    Docker / packageName := "grpc-echo-server",
    dockerUpdateLatest   := true
  )
  .settings(SbtTpolecat.options)
  .enablePlugins(Fs2Grpc, BuildInfoPlugin, DockerPlugin, JavaAppPackaging)
