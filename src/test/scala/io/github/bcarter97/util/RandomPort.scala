package io.github.bcarter97.util

import cats.effect.{Resource, Sync}

import java.net.ServerSocket

object RandomPort {
  def apply[F[_]](using F: Sync[F]): F[Int] =
    Resource.fromAutoCloseable(F.blocking(ServerSocket(0))).use { socket =>
      F.blocking {
        socket.setReuseAddress(true)
        socket.getLocalPort
      }
    }
}
