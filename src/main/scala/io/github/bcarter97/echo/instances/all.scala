package io.github.bcarter97.echo.instances

import cats.Show
import cats.syntax.all.*
import grpc.health.v1.{HealthCheckRequest, HealthCheckResponse}
import io.circe.Encoder
import io.circe.syntax.*
import io.github.bcarter97.echo.circe.Codec.given
import io.github.bcarter97.echo.v1.{ClientRequest, ServerRequest, ServerResponse}
import io.github.bcarter97.grpc.Context

object all {
  private def toShow[T : Encoder]: Show[T] = _.asJson.noSpaces

  given mapShow: Show[Map[String, String]] = toShow
  given contextShow: Show[Context]         = mapShow.contramap(_.value)

  given healthCheckRequestShow: Show[HealthCheckRequest]   = toShow
  given healthCheckResponseShow: Show[HealthCheckResponse] = toShow

  given clientRequestShow: Show[ClientRequest] = toShow

  given serverRequestShow: Show[ServerRequest]   = toShow
  given serverResponseShow: Show[ServerResponse] = toShow
}
