package io.github.bcarter97.echo.circe

import cats.Show
import grpc.health.v1.HealthCheckResponse.ServingStatus
import grpc.health.v1.{HealthCheckRequest, HealthCheckResponse}
import io.circe.Encoder
import io.circe.generic.semiauto
import io.circe.syntax.*
import io.github.bcarter97.echo.v1.{EchoRequest, EchoResponse}
import io.grpc.Status

object Codec {

  private def toShow[T : Encoder]: Show[T] = _.asJson.noSpaces

  given Encoder[Status] =
    Encoder.forProduct3[Status, String, Option[String], Option[String]]("code", "description", "cause") { status =>
      (
        status.getCode.name(),
        Option(status.getDescription),
        Option(status.getCause).map(_.getMessage)
      )
    }

  given Encoder[ServingStatus] = Encoder.encodeString.contramap(_.name)

  given Encoder[HealthCheckResponse] = semiauto.deriveEncoder[HealthCheckResponse]
  given Encoder[HealthCheckRequest]  = semiauto.deriveEncoder[HealthCheckRequest]

  given Show[HealthCheckRequest]  = toShow
  given Show[HealthCheckResponse] = toShow

  given Encoder[EchoRequest]  = semiauto.deriveEncoder[EchoRequest]
  given Encoder[EchoResponse] = semiauto.deriveEncoder[EchoResponse]

  given Show[EchoRequest]  = toShow
  given Show[EchoResponse] = toShow

  given Show[Map[String, String]] = _.asJson.noSpaces
}
