package main

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Random {
  def randomString(length: Int): String = {
    val SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    val salt = new StringBuilder
    val rnd = new scala.util.Random
    while (salt.length < length) { // length of the random string.
      val index = (rnd.nextFloat() * SALTCHARS.length).asInstanceOf[Int]
      salt.append(SALTCHARS.charAt(index))
    }
    val saltStr = salt.toString
    saltStr
  }

  def randomInt(count: Int): Int ={
    new scala.util.Random().nextInt(count)
  }

  def randomProductRequest() : String = """{"description":"""".stripMargin + Random.randomString(14) + """",
                                        "title":"""".stripMargin + Random.randomString(15) + """",
                                        "id":""".stripMargin + randomInt(999) + "}" 
}

class UserSimulation extends Simulation {
  val httpProtocol = http
    .baseUrl("https://fakestoreapi.com")

  val post = scenario("Post Product")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", Random.randomProductRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post Product")
        .post("/products")
          .body(StringBody("${postrequest}")).asJson
    )

  val get = scenario("Get Product")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", Random.randomProductRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post Product")
        .post("/products")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.id").saveAs("id"))
    )
    .exitHereIfFailed
    .exec(
      http("Get Product")
        .get("/products/${id}")
    )

  val put = scenario("Put Product")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", Random.randomProductRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post Product")
        .post("/products")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.id").saveAs("id"))
    )
    .exitHereIfFailed
    .exec(sessionPut => {
      val sessionPutUpdate = sessionPut.set("putrequest", Random.randomProductRequest())
      sessionPutUpdate
    })
    .exec(
      http("Put Product")
        .put("/products/${id}")
        .body(StringBody("${putrequest}")).asJson
    )

  val delete = scenario("Delete Product")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", Random.randomProductRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post Product")
        .post("/products")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.id").saveAs("id"))
    )
    .exitHereIfFailed
    .exec(sessionPut => {
      val sessionPutUpdate = sessionPut.set("putrequest", Random.randomProductRequest())
      sessionPutUpdate
    })
    .exec(
      http("Delete Product")
        .delete("/products/${id}")
    )

  setUp(post.inject(rampUsers(15).during(30.seconds)).protocols(httpProtocol),
    get.inject(rampUsers(15).during(30.seconds)).protocols(httpProtocol),
    put.inject(rampUsers(15).during(30.seconds)).protocols(httpProtocol),
    delete.inject(rampUsers(15).during(30.seconds)).protocols(httpProtocol))
}