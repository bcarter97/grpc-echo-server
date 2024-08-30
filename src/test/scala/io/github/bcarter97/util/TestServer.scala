package io.github.bcarter97.util

import cats.effect.{Async, Resource}
import fs2.grpc.syntax.all.*
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.{Server, ServerServiceDefinition}

object TestServer {
  def apply[F[_] : Async](port: Int, service: ServerServiceDefinition): Resource[F, Server] =
    for {
      server <- NettyServerBuilder
                  .forPort(port)
                  .addService(service)
                  .resource[F]
                  .evalMap(server => Async[F].delay(server.start()))
    } yield server
}
