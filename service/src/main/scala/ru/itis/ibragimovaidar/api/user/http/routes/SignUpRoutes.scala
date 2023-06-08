package ru.itis.ibragimovaidar.api.user.http.routes

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import org.http4s.{HttpRoutes, Uri}
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityEncoder._
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.headers.Location
import ru.itis.ibragimovaidar.api.user.config.ServiceConfig
import ru.itis.ibragimovaidar.api.user.errors.UserNameAlreadyInUse
import ru.itis.ibragimovaidar.api.user.http.dto.CreateUserRequest
import ru.itis.ibragimovaidar.api.user.services.UserService
import ru.itis.ibragimovaidar.api.user.validation.user
import ru.itis.ibragimovaidar.api.user.validation.user.CreateUserValidation

class SignUpRoutes(userService: UserService, serviceConfig: ServiceConfig) extends BaseRoutes with LazyLogging {

  private def handleCreateUserResponses(req: CreateUserRequest) = {
    //TODO maybe is better to move the location builder in a separate class
    def generateLocationForUser(id: Int): Uri =
      Uri.unsafeFromString(
        s"http://${serviceConfig.externalHost}/api/${serviceConfig.apiVersion}/users/$id"
      )
    userService.createUser(req).flatMap {
      case Right(id) =>
        Created(Location(generateLocationForUser(id)))
      case Left(UserNameAlreadyInUse) =>
        Conflict()
    }
  }

  override val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "signUp" =>
      for {
        json <- req.decodeJson[CreateUserRequest]
        validated = user.CreateUserValidation(json)
        response <- validated.fold(
          errors => {
            logger.error(s"Founded errors $errors for request with json $json")
            BadRequest(getErrorsResponse(errors))
          },
          valid => handleCreateUserResponses(valid)
        )
      } yield response

  }

}
