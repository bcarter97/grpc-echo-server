package io.github.bcarter97.echo.v1

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import cats.{MonadThrow, Show}
import io.github.bcarter97.echo.instances.all.given
import io.github.bcarter97.grpc.{Context, Metadata}
import io.grpc.{ServerServiceDefinition, Status}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax.*

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
  private def logged[F[_] : MonadThrow : Logger, A : Show](delegate: EchoFs2Grpc[F, A]) = new EchoFs2Grpc[F, A] {
    override def echo(request: EchoRequest, ctx: A): F[EchoResponse] =
      debug"Calling EchoService#echo with request ${request.show} and context ${ctx.show}" >> delegate
        .echo(request, ctx)
        .attemptTap {
          case Left(error)     => Logger[F].error(error)(s"EchoService#echo returned ${error.getMessage}")
          case Right(response) => Logger[F].info(s"EchoService#echo returned ${response.show}")
        }
  }

  def resource[F[_] : Async : Logger]: Resource[F, ServerServiceDefinition] =
    EchoFs2Grpc.serviceResource(logged[F, Context](EchoService()), Metadata.create[F])
}
