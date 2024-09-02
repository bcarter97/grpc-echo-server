package io.github.bcarter97.grpc

import cats.effect.Sync
import cats.syntax.all.*
import io.grpc.Metadata

import scala.jdk.CollectionConverters.*

object Context {
  def create[F[_]](using F: Sync[F])(metadata: Metadata): F[Map[String, String]] =
    for {
      keys <- F.delay(metadata.keys().asScala.toList)
      kvs  <- keys.flatTraverse(key => metadata.getF(key).map(_.map(key -> _).toList))
    } yield kvs.toMap

  def extract[F[_]](using F: Sync[F])(context: Map[String, String]): F[Metadata] =
    F.delay(Metadata()).flatMap(_.putAllF(context))
}

private def stringKey(key: String): Metadata.Key[String] = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)

extension (metadata: Metadata) {
  def getF[F[_] : Sync](key: String): F[Option[String]] =
    Sync[F].delay(metadata.get(stringKey(key))).attempt.map(_.toOption)

  def putF[F[_] : Sync](key: String, value: String): F[Metadata] =
    Sync[F].delay(metadata.put(stringKey(key), value)).as(metadata)

  def putAllF[F[_] : Sync](kvs: Iterable[(String, String)]): F[Metadata] =
    kvs.toList.traverse_((key, value) => putF(key, value)).as(metadata)
}
