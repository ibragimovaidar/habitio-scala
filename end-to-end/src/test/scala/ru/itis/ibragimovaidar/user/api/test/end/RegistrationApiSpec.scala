package ru.itis.ibragimovaidar.user.api.test.end

import cats.effect.IO
import io.circe.Json
import org.http4s.{Request, Uri}
import org.http4s.circe._
import org.http4s.dsl.io._
import ru.itis.ibragimovaidar.user.api.test.end.common.JsonBuilder

class RegistrationApiSpec extends SpecBase {

  "RegistrationApi" when {

    s"POST /api/$apiVersion/signUp is called" should {
      "return Created for a valid json request" in {
        val jsonBody = JsonBuilder.createRequestJson
        val req: Request[IO] =
          Request(method = POST, uri = Uri.unsafeFromString(createUserApiPath)).withEntity(jsonBody)
        client.status(req).map(value => value.code shouldBe 201).unsafeToFuture
      }

      "return UnProcessableEntity with a bad json request" in {
        val jsonBody = Json.obj("test" -> Json.fromString("test"))
        val req: Request[IO] =
          Request(method = POST, uri = Uri.unsafeFromString(createUserApiPath)).withEntity(jsonBody)
        client.status(req).map(value => value.code shouldBe 422).unsafeToFuture
      }

      "return Conflict if exist already a user with the same email" in {
        val jsonBody = JsonBuilder.createRequestJson
        val req: Request[IO] =
          Request(method = POST, uri = Uri.unsafeFromString(createUserApiPath)).withEntity(jsonBody)
        client.status(req).unsafeRunSync() //create the user

        client.status(req).map(value => value.code shouldBe 409).unsafeToFuture

      }
    }

  }

}
