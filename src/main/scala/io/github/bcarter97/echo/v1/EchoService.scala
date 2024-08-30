package io.github.bcarter97.echo.v1

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import io.github.bcarter97.grpc.mkMetadata
import io.grpc.{ServerServiceDefinition, Status}

import scala.concurrent.duration.*

private final class EchoImpl[F[_], A](using F: Async[F]) extends EchoFs2Grpc[F, A] {
  override def echo(request: EchoRequest, ctx: A): F[EchoResponse] = {
    val response = request match {
      case EchoRequest(0, delay) => EchoResponse(0, delay).pure[F]
      case EchoRequest(other, _) => Status.fromCodeValue(other).asRuntimeException().raiseError[F, EchoResponse]
    }

    F.sleep(request.delay.millis) >> response
  }
}

object EchoService {
  def resource[F[_] : Async]: Resource[F, ServerServiceDefinition] =
    EchoService(EchoImpl[F, Map[String, String]])

  def apply[F[_] : Async](impl: EchoFs2Grpc[F, Map[String, String]]): Resource[F, ServerServiceDefinition] =
    EchoFs2Grpc.serviceResource(impl, mkMetadata[F])
}
