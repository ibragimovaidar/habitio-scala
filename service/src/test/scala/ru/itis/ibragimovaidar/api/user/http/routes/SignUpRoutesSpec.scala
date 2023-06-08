package ru.itis.ibragimovaidar.api.user.http.routes

import org.http4s.Request
import org.http4s.implicits._
import org.mockito.MockitoSugar
import cats.effect.IO
import org.http4s.dsl.io._
import io.circe.generic.auto._
import org.http4s.circe._
import org.scalatest.OptionValues
import io.circe.syntax._
import ru.itis.ibragimovaidar.api.user.SpecBase
import ru.itis.ibragimovaidar.api.user.config.ServiceConfig
import ru.itis.ibragimovaidar.api.user.data.UserDataGenerator
import ru.itis.ibragimovaidar.api.user.errors.UserNameAlreadyInUse
import ru.itis.ibragimovaidar.api.user.services.UserService

class SignUpRoutesSpec extends SpecBase with MockitoSugar with OptionValues {

  private val userService = mock[UserService]
  private val serviceConfig = ServiceConfig("not-used", 8080, 999, "0.1", "localhost")
  private val registrationRoutes =
    new SignUpRoutes(userService, serviceConfig).routes.orNotFound

  private val createRequest = UserDataGenerator.generateCreateUserRequest

  "RegistrationRoutes" when {

    "POST /signUp is called" should {
      "return Created with a valid request" in {

        when(userService.createUser(createRequest)).thenReturn(IO.pure(Right(123)))
        val response = registrationRoutes
          .run(Request(method = POST, uri = uri"/signUp").withEntity(createRequest.asJson))
          .unsafeRunSync()

        response.status shouldBe Created

        val locationHeaderValue =
          response.headers.toList.find(h => h.name.toString == "Location").value.value

        locationHeaderValue.contains(serviceConfig.externalHost) shouldBe true
        locationHeaderValue.contains(s"api/${serviceConfig.apiVersion}/users") shouldBe true
      }
      "return Conflict when the request email exists already" in {
        when(userService.createUser(createRequest)).thenReturn(IO.pure(Left(UserNameAlreadyInUse)))
        val response = registrationRoutes
          .run(Request(method = POST, uri = uri"/signUp").withEntity(createRequest.asJson))
          .unsafeRunSync()

        response.status shouldBe Conflict

      }

      "return BadRequest if the request is invalid" in {
        val invalidReq = UserDataGenerator.generateCreateUserRequest.copy(gender = "invalid-gender")
        val response = registrationRoutes
          .run(Request(method = POST, uri = uri"/signUp").withEntity(invalidReq.asJson))
          .unsafeRunSync()

        response.status shouldBe BadRequest
      }
    }

  }

}
