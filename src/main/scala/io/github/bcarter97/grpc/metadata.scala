package io.github.bcarter97.grpc

import cats.effect.Sync
import cats.syntax.all.*
import io.grpc.Metadata

import scala.jdk.CollectionConverters.*

def mkMetadata[F[_]](using F: Sync[F]): Metadata => F[Map[String, String]] = metadata =>
  for {
    keys <- F.delay(metadata.keys().asScala.toList)
    keys <- keys.flatTraverse { key =>
              F.delay(metadata.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))).attempt.map {
                case Left(_)      => List.empty[(String, String)]
                case Right(value) => List(key -> value)
              }
            }
  } yield keys.toMap
