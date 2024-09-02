package io.github.bcarter97.echo.health

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import cats.{Applicative, MonadThrow, Show}
import fs2.Stream
import grpc.health.v1.{HealthCheckRequest, HealthCheckResponse, HealthFs2Grpc}
import io.github.bcarter97.echo.circe.Codec.given
import io.github.bcarter97.grpc.Context
import io.grpc.ServerServiceDefinition
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax.*

final class HealthService[F[_] : Applicative, A] extends HealthFs2Grpc[F, A] {
  override def check(request: HealthCheckRequest, ctx: A): F[HealthCheckResponse] =
    HealthCheckResponse(HealthCheckResponse.ServingStatus.SERVING).pure[F]

  override def watch(request: HealthCheckRequest, ctx: A): Stream[F, HealthCheckResponse] =
    Stream(HealthCheckResponse(HealthCheckResponse.ServingStatus.SERVICE_UNKNOWN))
}

object HealthService {
  private def logged[F[_] : MonadThrow : Logger, A : Show](delegate: HealthFs2Grpc[F, A]) =
    new HealthFs2Grpc[F, A] {
      override def check(request: HealthCheckRequest, ctx: A): F[HealthCheckResponse] =
        debug"Calling HealthFs2Grpc#check with ${request.show} and context ${ctx.show}" >> delegate
          .check(request, ctx)
          .attemptTap {
            case Left(error)     => Logger[F].error(error)(s"HealthService#check returned ${error.getMessage}")
            case Right(response) => Logger[F].info(s"HealthService#check returned ${response.show}")
          }

      override def watch(request: HealthCheckRequest, ctx: A): Stream[F, HealthCheckResponse] =
        delegate.watch(request, ctx)
    }

  def resource[F[_] : Async : Logger]: Resource[F, ServerServiceDefinition] =
    HealthFs2Grpc.serviceResource(logged[F, Map[String, String]](HealthService()), Context.create[F])
}
