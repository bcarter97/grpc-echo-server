package io.github.bcarter97.grpc

import cats.effect.Sync
import cats.syntax.all.*
import cats.{Eq, Show}
import io.grpc.Metadata as JMetadata

import scala.jdk.CollectionConverters.*

opaque type Context = Map[String, String]

object Context {
  def apply(underlying: Map[String, String]): Context = underlying

  extension (context: Context) {
    inline def value: Map[String, String] = context
  }

  val empty: Context  = Map.empty[String, String]
  given Show[Context] = Show.catsShowForMap[String, String]
  given Eq[Context]   = Eq.catsKernelEqForMap[String, String]
}

object Metadata {

  import syntax.*

  def create[F[_]](using F: Sync[F])(metadata: JMetadata): F[Context] =
    metadata.getAllF[F].map(Context.apply)

  def extract[F[_]](using F: Sync[F])(context: Context): F[JMetadata] =
    F.delay(JMetadata()).flatMap(_.putAllF(context.value))

  object syntax {
    private def stringKey(key: String): JMetadata.Key[String] = JMetadata.Key.of(key, JMetadata.ASCII_STRING_MARSHALLER)

    extension (metadata: JMetadata) {
      def keysF[F[_] : Sync]: F[List[String]] = Sync[F].delay(metadata.keys().asScala.toList)

      def getF[F[_] : Sync](key: String): F[Option[String]] =
        Sync[F].delay(metadata.get(stringKey(key))).attempt.map(_.toOption)

      def getAllF[F[_] : Sync]: F[Map[String, String]] =
        for {
          keys <- metadata.keysF[F]
          kvs  <- keys.flatTraverse(key => metadata.getF(key).map(_.map(key -> _).toList))
        } yield kvs.toMap

      def putF[F[_] : Sync](key: String, value: String): F[JMetadata] =
        Sync[F].delay(metadata.put(stringKey(key), value)).as(metadata)

      def putAllF[F[_] : Sync](kvs: Iterable[(String, String)]): F[JMetadata] =
        kvs.toList.foldLeftM(metadata) { case (metadata, (key, value)) => metadata.putF(key, value) }
    }
  }

}
