package io.github.bcarter97.echo.config

import cats.Show
import cats.effect.Sync
import cats.syntax.all.*
import com.comcast.ip4s.{Host, Port}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.*
import io.github.bcarter97.echo.config
import pureconfig.ConfigReader
import pureconfig.error.UserValidationFailed
import pureconfig.generic.semiauto.deriveReader
import pureconfig.module.ip4s.*

import java.net.InetSocketAddress

final case class Config(
    server: ServerConfig,
    client: Option[ClientConfig]
)

object Config {
  given ConfigReader[Config] = deriveReader[Config]

  given Show[Config] = _.asJson.noSpaces
}

trait HostConfig {
  def host: Host
  def port: Port
}

object HostConfig {
  extension (config: HostConfig) {
    def socketAddress[F[_] : Sync]: F[InetSocketAddress] =
      Sync[F].blocking(InetSocketAddress(config.host.toString, config.port.value))
  }
}

final case class ServerConfig(host: Host, port: Port) extends HostConfig

object ServerConfig {
  given ConfigReader[ServerConfig] = deriveReader[ServerConfig]

  given Show[ServerConfig] = _.asJson.noSpaces
}

final case class ClientConfig(host: Host, port: Port) extends HostConfig

object ClientConfig {
  given ConfigReader[Option[ClientConfig]] =
    ConfigReader
      .forProduct2[(Option[Host], Option[Port]), Option[Host], Option[Port]]("host", "port")((host, port) =>
        host -> port
      )
      .emap {
        case (Some(host), Some(port)) => ClientConfig(host, port).some.asRight
        case (None, None)             => none[ClientConfig].asRight
        case _                        => UserValidationFailed("Both host and port must be set, or neither").asLeft
      }

  given Show[ClientConfig] = _.asJson.noSpaces
}

private given Encoder[Host]         = Encoder.encodeString.contramap(_.toString)
private given Encoder[Port]         = Encoder.encodeInt.contramap(_.value)
private given Encoder[ServerConfig] = deriveEncoder[ServerConfig]
private given Encoder[ClientConfig] = deriveEncoder[ClientConfig]
private given Encoder[Config]       = deriveEncoder[Config]
