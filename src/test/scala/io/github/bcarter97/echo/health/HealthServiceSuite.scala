package io.github.bcarter97.echo.health

import cats.effect.IO
import grpc.health.v1.{HealthCheckRequest, HealthCheckResponse, HealthFs2Grpc}
import io.github.bcarter97.util.{RandomPort, TestClient, TestServer}
import munit.CatsEffectSuite
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger

class HealthServiceSuite extends CatsEffectSuite {

  private given Logger[IO] = NoOpLogger[IO]

  private val clientFixture =
    ResourceFunFixture(
      for {
        port    <- RandomPort[IO].toResource
        service <- HealthService.resource[IO]
        _       <- TestServer[IO](port, service)
        client  <- TestClient[IO, HealthFs2Grpc](port, HealthFs2Grpc)
      } yield client
    )

  clientFixture.test("return SERVING") { client =>
    client
      .check(HealthCheckRequest("dummy"), Map.empty)
      .assertEquals(HealthCheckResponse(HealthCheckResponse.ServingStatus.SERVING))
  }
}
