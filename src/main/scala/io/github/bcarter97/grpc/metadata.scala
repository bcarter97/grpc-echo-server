package io.github.bcarter97.grpc

import cats.effect.Sync
import cats.syntax.all.*
import io.grpc.Metadata

import scala.jdk.CollectionConverters.*

object Context {
  def create[F[_]](using F: Sync[F])(metadata: Metadata): F[Map[String, String]] =
    metadata.getAllF[F]

  def extract[F[_]](using F: Sync[F])(context: Map[String, String]): F[Metadata] =
    F.delay(Metadata()).flatMap(_.putAllF(context))
}

private def stringKey(key: String): Metadata.Key[String] = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)

extension (metadata: Metadata) {
  def keysF[F[_] : Sync]: F[List[String]] = Sync[F].delay(metadata.keys().asScala.toList)

  def getF[F[_] : Sync](key: String): F[Option[String]] =
    Sync[F].delay(metadata.get(stringKey(key))).attempt.map(_.toOption)

  def getAllF[F[_] : Sync]: F[Map[String, String]] =
    for {
      keys <- metadata.keysF[F]
      kvs  <- keys.flatTraverse(key => metadata.getF(key).map(_.map(key -> _).toList))
    } yield kvs.toMap

  def putF[F[_] : Sync](key: String, value: String): F[Metadata] =
    Sync[F].delay(metadata.put(stringKey(key), value)).as(metadata)

  def putAllF[F[_] : Sync](kvs: Iterable[(String, String)]): F[Metadata] =
    kvs.toList.foldLeftM(metadata) { case (metadata, (key, value)) => metadata.putF(key, value) }
}
