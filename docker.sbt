import scala.sys.process.Process

lazy val ensureDockerBuildx    = taskKey[Unit]("Ensure that docker buildx configuration exists")
lazy val dockerBuildWithBuildx = taskKey[Unit]("Build docker images using buildx")

ensureDockerBuildx := {
  if (Process("docker buildx inspect multi-arch-builder").! == 1) {
    Process("docker buildx create --use --name multi-arch-builder", baseDirectory.value).!
  }
}

dockerBuildWithBuildx := {
  streams.value.log("Building and pushing image with Buildx")
  dockerAliases.value.foreach { alias =>
    Process(
      "docker buildx build --platform=linux/arm64,linux/amd64 --push -t " +
        alias + " .",
      baseDirectory.value / "target" / "docker" / "stage"
    ).!
  }
}

Docker / publish := Def
  .sequential(
    Docker / publishLocal,
    ensureDockerBuildx,
    dockerBuildWithBuildx
  )
  .value
