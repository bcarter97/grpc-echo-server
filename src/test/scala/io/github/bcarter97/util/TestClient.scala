package io.github.bcarter97.util

import cats.effect.syntax.all.*
import cats.effect.{Async, Resource}
import fs2.grpc.GeneratedCompanion
import fs2.grpc.syntax.all.*
import io.github.bcarter97.grpc.{Context, Metadata}
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder

object TestClient {
  def apply[F[_] : Async, Service[*[_], _]](
      port: Int,
      companion: GeneratedCompanion[Service]
  ): Resource[F, Service[F, Context]] =
    for {
      channelBuilder <- Async[F].blocking(NettyChannelBuilder.forAddress("localhost", port).usePlaintext()).toResource
      channel        <- channelBuilder.resource[F]
      client         <- companion.mkClientResource(channel, Metadata.extract[F])
    } yield client
}
