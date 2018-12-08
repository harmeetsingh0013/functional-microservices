package com.knoldus.functionalconf2018.examples

import cats.effect.IO
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.{Matchers, WordSpecLike}

class ApplicationSpec extends WordSpecLike with Matchers {

    "Request" should {
        "should return greeting with Hello " in {
            val mockRequest = Request[IO](method = Method.GET, uri = Uri.uri("/hello/Singh") )
            val response = new Application().httpRoutes.run(mockRequest).value.unsafeRunSync()
            response.get.status should === (Status.Ok)
            response.get.as[String].unsafeRunSync() should === ("Hello Singh")
        }
    }
}
