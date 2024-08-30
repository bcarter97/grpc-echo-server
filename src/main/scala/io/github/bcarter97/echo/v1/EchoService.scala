package io.github.bcarter97.echo.v1

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import io.github.bcarter97.grpc.Context
import io.grpc.{ServerServiceDefinition, Status}

import scala.concurrent.duration.*

final class EchoService[F[_], A](using F: Async[F]) extends EchoFs2Grpc[F, A] {
  override def echo(request: EchoRequest, ctx: A): F[EchoResponse] = {
    val response = request match {
      case EchoRequest(0, delay)     => EchoResponse(0, delay).pure[F]
      case EchoRequest(other, delay) =>
        val status      = Status.fromCodeValue(other)
        val description = {
          val code = status.getCode.toString
          s"$code after ${delay.millis}"
        }
        status.withDescription(description).asRuntimeException().raiseError[F, EchoResponse]
    }

    F.sleep(request.delay.millis) >> response
  }
}

object EchoService {
  def resource[F[_] : Async]: Resource[F, ServerServiceDefinition] =
    EchoFs2Grpc.serviceResource(EchoService(), Context.create[F])
}
