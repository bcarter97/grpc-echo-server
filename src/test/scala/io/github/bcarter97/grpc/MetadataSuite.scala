package io.github.bcarter97.grpc

import cats.effect.IO
import io.grpc.Metadata
import munit.CatsEffectSuite

class MetadataSuite extends CatsEffectSuite {

  test("mkMetadata should convert Metadata into a Map, dropping keys that can't be decoded") {
    val expected = Map(
      "key1" -> "value1",
      "key2" -> "value2"
    )

    for {
      metadata <-
        Metadata()
          .putAllF[IO](expected)
          .flatTap(metadata =>
            IO(
              metadata.put(
                Metadata.Key.of(s"bytes${Metadata.BINARY_HEADER_SUFFIX}", Metadata.BINARY_BYTE_MARSHALLER),
                Array.empty[Byte]
              )
            )
          )
      result   <- mkMetadata[IO](metadata)
    } yield assertEquals(result, expected)
  }

}
