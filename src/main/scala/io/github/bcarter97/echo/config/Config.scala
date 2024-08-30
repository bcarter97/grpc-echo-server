package io.github.bcarter97.echo.config

import cats.Show
import cats.effect.Sync
import com.comcast.ip4s.{Host, Port}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.*
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader
import pureconfig.module.ip4s.*

import java.net.InetSocketAddress

final case class Config(host: Host, port: Port) {
  def socketAddress[F[_] : Sync]: F[InetSocketAddress] = Sync[F].blocking(InetSocketAddress(host.toString, port.value))
}

object Config {
  given ConfigReader[Config] = deriveReader[Config]

  private given Encoder[Host]   = Encoder.encodeString.contramap(_.toString)
  private given Encoder[Port]   = Encoder.encodeInt.contramap(_.value)
  private given Encoder[Config] = deriveEncoder[Config]

  given Show[Config] = _.asJson.noSpaces
}
