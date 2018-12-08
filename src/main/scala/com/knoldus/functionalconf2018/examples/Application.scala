package com.knoldus.functionalconf2018.examples

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.transactor.Transactor
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.{HttpService, _}
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.io.{->, /, Ok, POST, Root, _}
import org.http4s.headers.`Content-Type`
import org.http4s.server.blaze.BlazeBuilder
import scala.concurrent.ExecutionContext.Implicits.global


case class User(id : Option[Long], name : String)

object User {
    implicit val encoder = jsonOf[IO, User]

}
case class Hello(greeting: User)

class UserRepo(transactor : Transactor[IO]) {

    def dropTable : ConnectionIO[Int] =
        sql"""
            DROP TABLE IF EXISTS user
        """.update.run

    def createTable : ConnectionIO[Int] =
        sql"""
           CREATE TABLE user (
             id Long PRIMARY KEY AUTO_INCREMENT,
             name VARCHAR(20) NOT NULL UNIQUE
           )
        """.update.run

    def addUser(user : User) : IO[User] =
        sql"""INSERT INTO user (name) VALUES (${user.name})""".update.withUniqueGeneratedKeys[Long]("id")
            .transact(transactor).map(id => user.copy(id = Some(id)))

    def getUsers : Stream[IO, User] = sql"""
            SELECT * FROM user
        """.query[User].stream.transact(transactor)
}

class Application(userRepo : UserRepo) {

    private val PathPrefix = "/"

    val httpRoutes : HttpService[IO] = HttpService[IO] {
        case req @ POST -> Root / "user" => for {
            user <- req.as[User]
            updatedUser <- userRepo.addUser(user)
            resp <- Ok(Hello(updatedUser).asJson)
        } yield resp

        case GET -> Root / "users" =>
            Ok(
                Stream("[") ++
                    userRepo.getUsers.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"),
                `Content-Type`(MediaType.`application/json`)
            )
    }
}

object Application extends StreamApp[IO] {

    val transactor : IO[Transactor[IO]] = HikariTransactor.newHikariTransactor[IO](
        "org.h2.Driver",
        "jdbc:h2:mem:users;DB_CLOSE_DELAY=-1",
        "sa", ""
    )

    override def stream(args : List[String], requestShutdown : IO[Unit]) : Stream[IO, ExitCode] = for {
        tr <- Stream.eval(transactor)
        userRepo = new UserRepo(tr)
        _ <- Stream.eval((userRepo.dropTable, userRepo.createTable).mapN(_ + _).transact(tr))
        app = new Application(userRepo)
        exitCode <- BlazeBuilder[IO]
            .bindHttp(8080, "localhost")
            .mountService(app.httpRoutes, app.PathPrefix)
            .serve
    } yield exitCode
}