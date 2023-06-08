package ru.itis.ibragimovaidar.api.user.http.routes

import cats.effect.IO
import io.circe.Json
import org.http4s.{Request, Uri}
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.{BeforeAndAfterEach, EitherValues, OptionValues}
import ru.itis.ibragimovaidar.api.user.SpecBase
import ru.itis.ibragimovaidar.api.user.data.UserDataGenerator
import ru.itis.ibragimovaidar.api.user.errors.UserNotFound
import ru.itis.ibragimovaidar.api.user.http.dto.SearchUsersResponse
import ru.itis.ibragimovaidar.api.user.services.UserService

import scala.util.Random

class UserRoutesSpec
    extends SpecBase
    with MockitoSugar
    with ArgumentMatchersSugar
    with OptionValues
    with EitherValues
    with BeforeAndAfterEach {

  private val updateRequest = UserDataGenerator.generateUpdateUserRequest
  private val searchUsersResponse = UserDataGenerator.generateUserSearchResponse

  private val userService = mock[UserService]
  private val userRoutes = new UserRoutes(userService, mockAuthMiddleWare).routes.orNotFound

  "UserRoutes" when {
    "GET /users/{id} is called" should {
      "return NotFound if the id doesn't exist" in {
        when(userService.findUser(999)).thenReturn(IO.pure(None))
        val response = userRoutes
          .run(Request(uri = uri"/users/999"))
          .unsafeRunSync()

        response.status shouldBe NotFound
      }

      "return Ok with the json if the id exist" in {
        val userGenerated = UserDataGenerator.generateUserDetailResponse
        when(userService.findUser(999))
          .thenReturn(IO.pure(Some(userGenerated)))
        val response = userRoutes
          .run(Request(uri = uri"/users/999"))
          .unsafeRunSync()

        val hcursor = response.as[Json].unsafeRunSync.hcursor
        hcursor.get[String]("firstname").right.value shouldBe userGenerated.firstname
        hcursor.get[String]("lastname").right.value shouldBe userGenerated.lastname
        hcursor.get[String]("email").right.value shouldBe userGenerated.email
        hcursor.get[String]("gender").right.value shouldBe userGenerated.gender
        hcursor.get[String]("dateOfBirth").right.value shouldBe userGenerated.dateOfBirth
      }

    }

    "GET /users?search={query}" should {
      "return Ok with the list of users that meets the criteria" in {
        val query = Random.nextString(5)
        when(userService.searchUsers(anyInt, any[String]))
          .thenReturn(IO.pure(SearchUsersResponse(Seq(searchUsersResponse))))

        val response = userRoutes
          .run(Request(uri = Uri.unsafeFromString("/users?search=$query")))
          .unsafeRunSync()

        response.status shouldBe Ok

        val json = response.as[Json].unsafeRunSync.hcursor.as[SearchUsersResponse].right.get
        json.items shouldBe Seq(searchUsersResponse)

      }
      "return Ok with empty list when the query doesn't match with any user" in {
        val query = Random.nextString(5)
        when(userService.searchUsers(anyInt, any[String]))
          .thenReturn(IO.pure(SearchUsersResponse(Seq())))
        val response = userRoutes
          .run(Request(uri = Uri.unsafeFromString("/users?search=$query")))
          .unsafeRunSync()

        response.status shouldBe Ok

        val json = response.as[Json].unsafeRunSync.hcursor.as[SearchUsersResponse].right.get
        json.items.isEmpty shouldBe true

      }
    }

    "DELETE /users/{id} is called" should {
      "return NoContent if the id exist" in {
        when(userService.deleteUser(999))
          .thenReturn(IO.pure(Right()))
        val response = userRoutes
          .run(Request(uri = uri"/users/999", method = DELETE))
          .unsafeRunSync()

        response.status shouldBe NoContent
      }

      "return NotFound if the id doesn't exist" in {
        when(userService.deleteUser(999))
          .thenReturn(IO.pure(Left(UserNotFound)))
        val response = userRoutes
          .run(Request(uri = uri"/users/999", method = DELETE))
          .unsafeRunSync()

        response.status shouldBe NotFound

      }
    }

    "PUT /users/{id} is called" should {
      "return BadRequest if the json is not valid" in {
        val invalidReq = UserDataGenerator.generateUpdateUserRequest.copy(
          gender = "invalid-gender",
          firstname = "",
          lastname = ""
        )
        val response = userRoutes
          .run(Request(uri = uri"/users/999", method = PUT).withEntity(invalidReq.asJson))
          .unsafeRunSync()

        response.status shouldBe BadRequest

      }
      "return NoContent if the id exist" in {
        when(userService.updateUser(999, updateRequest))
          .thenReturn(IO.pure(Right()))
        val response = userRoutes
          .run(Request(uri = uri"/users/999", method = PUT).withEntity(updateRequest.asJson))
          .unsafeRunSync()

        response.status shouldBe NoContent

      }
      "return NotFound if the id doesn't exist" in {
        when(userService.updateUser(999, updateRequest))
          .thenReturn(IO.pure(Left(UserNotFound)))
        val response = userRoutes
          .run(Request(uri = uri"/users/999", method = PUT).withEntity(updateRequest.asJson))
          .unsafeRunSync()

        response.status shouldBe NotFound

      }
    }

  }

}
