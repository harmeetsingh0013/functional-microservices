package com.knoldus.functionalconf2018.examples

import cats.effect.IO
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends App {

    // Memoization
    def addToGaugeIO(i: Int): IO[Int] = IO {
        println("Execute addToGaugeIO ... ")
        i
    }

    println("IO addToGaugeIO First")
    val result1 = for {
        a <- addToGaugeIO(13)
        b <- addToGaugeIO(13)
    } yield a + b

    println(result1.unsafeRunSync())

    println("IO addToGaugeIO Second")
    val x = addToGaugeIO(13)
    val result2 = for {
        a <- x
        b <- x
    } yield a + b

    println(result2.unsafeRunSync())

    // ***************************************************

    def addToGaugeFuture(i : Int): Future[Int] = Future {
        println("Execute addToGaugeFuture ... ")
        i
    }

    println("Future addToGaugeFuture First .... ")
    val result3 = for {
        a <- addToGaugeFuture(13)
        b <- addToGaugeFuture(13)
    } yield a + b

    result3.foreach(println)

    Thread.sleep(1000)

    println("Future addToGaugeFuture Second .... ")
    val y = addToGaugeFuture(13)
    val result4 = for {
        a <- y
        b <- y
    } yield a + b

    result4.foreach(println)

    Thread.sleep(1000)
}
