package io.github.bcarter97.echo.circe

import grpc.health.v1.HealthCheckResponse.ServingStatus
import grpc.health.v1.{HealthCheckRequest, HealthCheckResponse}
import io.circe.Encoder
import io.circe.generic.semiauto
import io.github.bcarter97.echo.v1.{ServerRequest, ServerResponse}
import io.grpc.Status

object Codec {
  given Encoder[Status] =
    Encoder.forProduct3[Status, String, Option[String], Option[String]]("code", "description", "cause") { status =>
      (
        status.getCode.name(),
        Option(status.getDescription),
        Option(status.getCause).flatMap(e => Option(e.getMessage))
      )
    }

  given Encoder[ServingStatus] = Encoder.encodeString.contramap(_.name)

  given Encoder[HealthCheckResponse] = semiauto.deriveEncoder[HealthCheckResponse]
  given Encoder[HealthCheckRequest]  = semiauto.deriveEncoder[HealthCheckRequest]

  given Encoder[ServerRequest]  = semiauto.deriveEncoder[ServerRequest]
  given Encoder[ServerResponse] = semiauto.deriveEncoder[ServerResponse]
}
