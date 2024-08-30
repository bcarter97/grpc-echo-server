val scala3Version = "3.5.0"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = project
  .in(file("."))
  .settings(
    name              := "grpc-echo-server",
    scalaVersion      := scala3Version,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.4"
    ),
    run / fork        := true
  )
  .settings(SbtTpolecat.options)
  .enablePlugins(Fs2GrpcPlugin)
