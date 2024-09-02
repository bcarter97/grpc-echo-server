package io.github.bcarter97.echo.instances

import cats.Show
import grpc.health.v1.{HealthCheckRequest, HealthCheckResponse}
import io.circe.Encoder
import io.circe.syntax.*
import io.github.bcarter97.echo.circe.Codec.given
import io.github.bcarter97.echo.v1.{EchoRequest, EchoResponse}

object all {
  private def toShow[T : Encoder]: Show[T] = _.asJson.noSpaces

  given Show[Map[String, String]] = toShow

  given Show[HealthCheckRequest]  = toShow
  given Show[HealthCheckResponse] = toShow

  given Show[EchoRequest]  = toShow
  given Show[EchoResponse] = toShow
}
