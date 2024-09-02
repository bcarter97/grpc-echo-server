package io.github.bcarter97.grpc

import cats.effect.IO
import io.grpc.Metadata
import munit.CatsEffectSuite

class ContextSuite extends CatsEffectSuite {

  test("Context.create should convert Metadata into a Map, dropping keys that can't be decoded") {
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
      result   <- Context.create[IO](metadata)
    } yield assertEquals(result, expected)
  }

  test("roundtrip valid key-value pairs") {
    val expected = Map(
      "key1" -> "value1",
      "key2" -> "value2"
    )

    for {
      extracted <- Context.extract[IO](expected)
      created   <- Context.create[IO](extracted)
    } yield assertEquals(created, expected)
  }
}
