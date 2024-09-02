package io.github.bcarter97.grpc

import cats.effect.IO
import io.github.bcarter97.grpc.Metadata.syntax.*
import io.grpc.Metadata as JMetadata
import munit.CatsEffectSuite

class ContextSuite extends CatsEffectSuite {

  test("Context.create should convert JMetadata into a Map, dropping keys that can't be decoded") {
    val expected = Map(
      "key1" -> "value1",
      "key2" -> "value2"
    )

    for {
      metadata <-
        JMetadata()
          .putAllF[IO](expected)
          .flatTap(metadata =>
            IO(
              metadata.put(
                JMetadata.Key.of(s"bytes${JMetadata.BINARY_HEADER_SUFFIX}", JMetadata.BINARY_BYTE_MARSHALLER),
                Array.empty[Byte]
              )
            )
          )
      result   <- Metadata.create[IO](metadata)
    } yield assertEquals(result.value, expected)
  }

  test("roundtrip valid key-value pairs") {
    val expected = Map(
      "key1" -> "value1",
      "key2" -> "value2"
    )

    for {
      extracted <- Metadata.extract[IO](Context(expected))
      created   <- Metadata.create[IO](extracted)
    } yield assertEquals(created.value, expected)
  }
}
