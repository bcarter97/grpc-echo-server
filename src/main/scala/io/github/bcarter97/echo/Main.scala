package io.github.bcarter97.echo

import buildinfo.BuildInfo
import cats.effect.{IO, IOApp, Resource}
import cats.syntax.all.*
import fs2.grpc.syntax.all.*
import io.github.bcarter97.echo.config.Config
import io.github.bcarter97.echo.health.HealthService
import io.github.bcarter97.echo.v1.EchoService
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.{Server, ServerServiceDefinition}
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.syntax.*
import org.typelevel.log4cats.{Logger, LoggerFactory}
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax.*

import scala.jdk.CollectionConverters.*

object Main extends IOApp.Simple {
  private given LoggerFactory[IO] = Slf4jFactory.create[IO]
  given Logger[IO]                = LoggerFactory[IO].getLogger

  val serviceResource: Resource[IO, List[ServerServiceDefinition]] =
    for {
      echoService       <- EchoService.resource[IO]
      healthService     <- HealthService.resource[IO]
      reflectionService <- IO(ProtoReflectionService.newInstance().bindService()).toResource
    } yield List(echoService, healthService, reflectionService)

  val server: Resource[IO, Server] =
    for {
      config   <- ConfigSource.default.at("grpc-server").loadF[IO, Config]().toResource
      _        <- info"Loaded config ${config.show}".toResource
      address  <- config.socketAddress[IO].toResource
      executor <- IO.executor.toResource
      services <- serviceResource
      server   <- NettyServerBuilder
                    .forAddress(address)
                    .executor(executor)
                    .addServices(services.asJava)
                    .resource[IO]
                    .evalMap(server => IO(server.start()))
    } yield server

  override def run: IO[Unit] =
    info"Running ${BuildInfo.toString}" >> server.useForever
}
