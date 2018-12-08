package com.knoldus.functionalconf2018.examples

import cats.effect.IO
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.server.blaze.BlazeBuilder
import scala.concurrent.ExecutionContext.Implicits.global


case class User(name: String)
object User {
    implicit val encoder =  jsonOf[IO, User]
}

case class Hello(greeting: String)

class Application {

    private val PathPrefix = "/"

    val httpRoutes : HttpService[IO] = HttpService[IO] {
        case req @ POST -> Root / "user" => for {
            user: User <- req.as[User]
            resp <- Ok(Hello(user.name).asJson)
        } yield resp
    }
}

object Application extends StreamApp[IO] {

    val app = new Application
    import app._

    override def stream(args : List[String], requestShutdown : IO[Unit]) : Stream[IO, ExitCode] =
        BlazeBuilder[IO]
            .bindHttp(8080, "localhost")
            .mountService(httpRoutes, PathPrefix)
            .serve
}
