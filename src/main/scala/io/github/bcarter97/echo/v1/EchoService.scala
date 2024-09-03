package io.github.bcarter97.echo.v1

import cats.effect.syntax.all.*
import cats.effect.{Async, Resource}
import cats.syntax.all.*
import cats.{MonadThrow, Show}
import fs2.grpc.syntax.all.*
import io.github.bcarter97.echo.config.ClientConfig
import io.github.bcarter97.echo.instances.all.given
import io.github.bcarter97.grpc.{Context, Metadata}
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import io.grpc.{ManagedChannel, ServerServiceDefinition, Status}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax.*

import java.net.SocketAddress
import scala.concurrent.duration.*

final class SimpleEchoService[F[_], A](using F: Async[F]) extends EchoFs2Grpc[F, A] {
  override def echo(request: ServerRequest, ctx: A): F[ServerResponse] = {
    val response = request match {
      case ServerRequest(0, delay)     => ServerResponse(0, delay).pure[F]
      case ServerRequest(other, delay) =>
        val status      = Status.fromCodeValue(other)
        val description = {
          val code = status.getCode.toString
          s"$code after ${delay.millis}"
        }
        status.withDescription(description).asRuntimeException().raiseError[F, ServerResponse]
    }

    F.sleep(request.delay.millis) >> response
  }
}

object SimpleEchoService {
  private def logged[F[_] : MonadThrow : Logger, A : Show](delegate: EchoFs2Grpc[F, A]) = new EchoFs2Grpc[F, A] {
    override def echo(request: ServerRequest, ctx: A): F[ServerResponse] =
      debug"Calling SimpleEchoService#echo with request ${request.show} and context ${ctx.show}" >> delegate
        .echo(request, ctx)
        .attemptTap {
          case Left(error)     => Logger[F].error(error)(s"EchoService#echo returned ${error.getMessage}")
          case Right(response) => Logger[F].info(s"EchoService#echo returned ${response.show}")
        }
  }

  def resource[F[_] : Async : Logger]: Resource[F, ServerServiceDefinition] =
    EchoFs2Grpc.serviceResource(logged[F, Context](SimpleEchoService()), Metadata.create[F])
}

final class PropagatingEchoService[F[_], A](client: EchoFs2Grpc[F, A]) extends EchoFs2Grpc[F, A] {
  override def echo(request: ServerRequest, ctx: A): F[ServerResponse] = client.echo(request, ctx)
}

object PropagatingEchoService {
  private def logged[F[_] : MonadThrow : Logger, A : Show](delegate: EchoFs2Grpc[F, A]) = new EchoFs2Grpc[F, A] {
    override def echo(request: ServerRequest, ctx: A): F[ServerResponse] =
      debug"Calling PropagatingEchoService#echo with request ${request.show} and context ${ctx.show}" >> delegate
        .echo(request, ctx)
        .attemptTap {
          case Left(error)     => Logger[F].error(error)(s"EchoService#echo returned ${error.getMessage}")
          case Right(response) => Logger[F].info(s"EchoService#echo returned ${response.show}")
        }
  }

  def resource[F[_] : Async : Logger](config: ClientConfig): Resource[F, ServerServiceDefinition] =
    EchoClient(config).flatMap(resource)

  def resource[F[_] : Async : Logger](client: EchoFs2Grpc[F, Context]): Resource[F, ServerServiceDefinition] =
    EchoFs2Grpc.serviceResource(logged[F, Context](PropagatingEchoService(client)), Metadata.create[F])
}

object EchoClient {
  def apply[F[_] : Async](config: ClientConfig): Resource[F, EchoFs2Grpc[F, Context]] =
    config.socketAddress[F].toResource.flatMap(EchoClient.apply)

  def apply[F[_] : Async](socketAddress: SocketAddress): Resource[F, EchoFs2Grpc[F, Context]] =
    for {
      channelBuilder <- Async[F].blocking(NettyChannelBuilder.forAddress(socketAddress).usePlaintext()).toResource
      channel        <- channelBuilder.resource[F]
      client         <- EchoClient(channel)
    } yield client

  def apply[F[_] : Async](channel: ManagedChannel): Resource[F, EchoFs2Grpc[F, Context]] =
    EchoFs2Grpc.mkClientResource(channel, Metadata.extract[F])
}
