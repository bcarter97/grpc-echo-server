package io.github.bcarter97.echo.v1

import cats.Id
import cats.effect.testkit.TestControl
import cats.effect.{IO, Outcome}
import cats.syntax.all.*
import io.github.bcarter97.util.{RandomPort, TestClient, TestServer}
import io.grpc.StatusRuntimeException
import munit.CatsEffectSuite

import scala.concurrent.duration.*

class EchoServiceSuite extends CatsEffectSuite {

  private val liveClientFixture =
    ResourceFunFixture(
      for {
        port    <- RandomPort[IO].toResource
        service <- EchoService.resource[IO]
        _       <- TestServer[IO](port, service)
        client  <- TestClient[IO, EchoFs2Grpc](port, EchoFs2Grpc)
      } yield client
    )

  liveClientFixture.test("return an OK response") { client =>
    client
      .echo(EchoRequest(0, 0), Map.empty)
      .assertEquals(EchoResponse(0, 0))
  }

  liveClientFixture.test("return an Error response") { client =>
    (1 to 16).toList.traverse { code =>
      client
        .echo(EchoRequest(code, 0), Map.empty)
        .attempt
        .map(maybeError =>
          assertEquals(maybeError.leftMap(_.asInstanceOf[StatusRuntimeException].getStatus.getCode.value()), Left(code))
        )

    }
  }

  test("return a response after a delay") {
    val result = EchoService[IO, Map[String, String]]().echo(EchoRequest(0, 100), Map.empty)

    TestControl.execute(result).flatMap { control =>
      for {
        _ <- assertIO(control.results, None)
        _ <- control.tick
        _ <- assertIO(control.nextInterval, 100.millis)
        _ <- control.tickAll
        _ <- assertIO(control.results, Some(Outcome.Succeeded(Id(EchoResponse(0, 100)))))
      } yield ()
    }
  }
}
