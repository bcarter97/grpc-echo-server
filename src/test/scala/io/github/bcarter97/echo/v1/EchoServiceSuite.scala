package io.github.bcarter97.echo.v1

import cats.Id
import cats.effect.testkit.TestControl
import cats.effect.{IO, Outcome}
import cats.syntax.all.*
import io.github.bcarter97.grpc.Context
import io.github.bcarter97.util.{RandomPort, TestClient, TestServer}
import io.grpc.StatusRuntimeException
import munit.CatsEffectSuite
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger

import scala.concurrent.duration.*

class EchoServiceSuite extends CatsEffectSuite {

  private given Logger[IO] = NoOpLogger[IO]

  private val liveClientFixture =
    ResourceFunFixture(
      for {
        port    <- RandomPort[IO].toResource
        service <- SimpleEchoService.resource[IO]
        _       <- TestServer[IO](port, service)
        client  <- TestClient[IO, EchoFs2Grpc](port, EchoFs2Grpc)
      } yield client
    )

  liveClientFixture.test("return an OK response") { client =>
    client
      .echo(ServerRequest(0, 0), Context.empty)
      .assertEquals(ServerResponse(0, 0))
  }

  liveClientFixture.test("return an Error response") { client =>
    (1 to 16).toList.traverse { code =>
      client
        .echo(ServerRequest(code, 0), Context.empty)
        .attempt
        .map(maybeError =>
          assertEquals(maybeError.leftMap(_.asInstanceOf[StatusRuntimeException].getStatus.getCode.value()), Left(code))
        )

    }
  }

  test("return a response after a delay") {
    val result = SimpleEchoService[IO, Context]().echo(ServerRequest(0, 100), Context.empty)

    TestControl.execute(result).flatMap { control =>
      for {
        _ <- assertIO(control.results, None)
        _ <- control.tick
        _ <- assertIO(control.nextInterval, 100.millis)
        _ <- control.tickAll
        _ <- assertIO(control.results, Some(Outcome.Succeeded(Id(ServerResponse(0, 100)))))
      } yield ()
    }
  }
}
