package ru.itis.ibragimovaidar.api.user.http.routes

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe._
import io.circe.generic.auto._
import org.http4s.server.AuthMiddleware
import ru.itis.ibragimovaidar.api.user.errors.UserNotFound
import ru.itis.ibragimovaidar.api.user.http.authentication.UserInfo
import ru.itis.ibragimovaidar.api.user.http.json.UpdateUserRequest
import ru.itis.ibragimovaidar.api.user.services.UserService
import ru.itis.ibragimovaidar.api.user.validation.user
import ru.itis.ibragimovaidar.api.user.validation.user.UpdateUserValidation

class UserRoutes(
    userService: UserService,
    authMiddleware: AuthMiddleware[IO, UserInfo]
) extends BaseRoutes
    with LazyLogging {

  private object SearchQueryParamMatcher extends QueryParamDecoderMatcher[String]("search")

  private def handleUpdateUserResponse(id: Int, req: UpdateUserRequest) = {
    userService.updateUser(id, req).flatMap {
      case Right(_) =>
        NoContent()
      case Left(UserNotFound) =>
        NotFound()
    }
  }

  private val authedRoutes: AuthedRoutes[UserInfo, IO] = AuthedRoutes.of {
    case GET -> Root / "users" :? SearchQueryParamMatcher(search) as user if user.enabled =>
      for {
        result <- userService.searchUsers(user.id, search)
        response <- Ok(result)
      } yield response

    case GET -> Root / "users" / IntVar(userId) as user if user.enabled =>
      for {
        result <- userService.findUser(userId)
        response <- result.fold(NotFound())(value => Ok(value))
      } yield response
    case DELETE -> Root / "users" / IntVar(userId) as user if user.enabled =>
      for {
        result <- userService.deleteUser(userId)
        response <- result.fold(_ => NotFound(), _ => NoContent())
      } yield response
    case req @ PUT -> Root / "users" / IntVar(userId) as user if user.enabled =>
      for {
        json <- req.req.decodeJson[UpdateUserRequest]
        validated = user.UpdateUserValidation(json)
        response <- validated.fold(
          errors => BadRequest(getErrorsResponse(errors)),
          valid => handleUpdateUserResponse(userId, valid)
        )
      } yield response

  }

  override val routes: HttpRoutes[IO] = authMiddleware(authedRoutes)

}
