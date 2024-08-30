package io.github.bcarter97.echo.health

import cats.Applicative
import cats.effect.{Async, Resource}
import cats.syntax.all.*
import fs2.Stream
import grpc.health.v1.{HealthCheckRequest, HealthCheckResponse, HealthFs2Grpc}
import io.github.bcarter97.grpc.Context
import io.grpc.ServerServiceDefinition

final class HealthService[F[_] : Applicative, A] extends HealthFs2Grpc[F, A] {
  override def check(request: HealthCheckRequest, ctx: A): F[HealthCheckResponse] =
    HealthCheckResponse(HealthCheckResponse.ServingStatus.SERVING).pure[F]

  override def watch(request: HealthCheckRequest, ctx: A): Stream[F, HealthCheckResponse] =
    Stream(HealthCheckResponse(HealthCheckResponse.ServingStatus.SERVICE_UNKNOWN))
}

object HealthService {
  def resource[F[_] : Async]: Resource[F, ServerServiceDefinition] =
    HealthFs2Grpc.serviceResource(HealthService(), Context.create[F])
}
