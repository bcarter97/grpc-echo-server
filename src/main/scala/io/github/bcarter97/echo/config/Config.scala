package io.github.bcarter97.echo.config

import cats.effect.Sync
import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader
import pureconfig.module.ip4s.*

import java.net.InetSocketAddress

final case class Config(host: Host, port: Port) {
  def socketAddress[F[_] : Sync]: F[InetSocketAddress] = Sync[F].blocking(InetSocketAddress(host.toString, port.value))
}

object Config {
  given ConfigReader[Config] = deriveReader[Config]
}
