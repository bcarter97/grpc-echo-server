package io.github.bcarter97.echo

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  override def run: IO[Unit] = IO.println("Hello world")
}
