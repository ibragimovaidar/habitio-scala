package ru.itis.ibragimovaidar.user.api.test.end.common

import io.circe.Json

import scala.util.Random

object JsonBuilder {

  def createRequestJson: Json =
    Json.obj(
      "firstname" -> Json.fromString(Random.nextString(5)),
      "lastname" -> Json.fromString(Random.nextString(5)),
      "email" -> Json.fromString(s"test${Random.nextInt}@test.com"),
      "password" -> Json.fromString(s"password-${Random.nextInt}"),
      "gender" -> Json.fromString("MALE"),
      "dateOfBirth" -> Json.fromString("07-09-1993")
    )

  def updateRequestJson: Json =
    Json.obj(
      "firstname" -> Json.fromString(Random.nextString(5)),
      "lastname" -> Json.fromString(Random.nextString(5)),
      "gender" -> Json.fromString("MALE"),
      "dateOfBirth" -> Json.fromString("07-09-1993")
    )

}
