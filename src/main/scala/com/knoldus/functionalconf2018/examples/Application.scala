package com.knoldus.functionalconf2018.examples


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Application extends App {

    // Execute Eagerly
    val hello = Future { println("Hello") }
    val world = Future { println("World") }

    //    hello.flatMap(_ => world)

    Thread.sleep(100)
}
