package io.github.bcarter97.echo

import cats.Monad
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener
import io.grpc.{Metadata, ServerCall, ServerCallHandler, ServerInterceptor}
import org.typelevel.log4cats.LoggerFactory

class OtelServerInterceptor[F[_] : LoggerFactory : Monad](dispatcher: Dispatcher[F]) extends ServerInterceptor {
  private val logger = LoggerFactory[F].getLogger

  override def interceptCall[ReqT, RespT](
      call: ServerCall[ReqT, RespT],
      headers: Metadata,
      next: ServerCallHandler[ReqT, RespT]
  ): ServerCall.Listener[ReqT] =
    new SimpleForwardingServerCallListener[ReqT](next.startCall(call, headers)) {
      override def onMessage(message: ReqT): Unit =
        dispatcher.unsafeRunSync {
          for {
            _ <- logger.debug(s"Starting call: $call with headers: $headers, next is $next")
            _ <- logger.debug(s"Method descriptor: ${call.getMethodDescriptor.getFullMethodName}")
          } yield super.onMessage(message)
        }
    }

}
