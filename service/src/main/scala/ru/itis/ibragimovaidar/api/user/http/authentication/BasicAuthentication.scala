package ru.itis.ibragimovaidar.api.user.http.authentication

import cats.effect.IO
import ru.itis.ibragimovaidar.api.user.http.dto.{ErrorItem, ErrorsResponse}
import ru.itis.ibragimovaidar.api.user.repository.UserRepository

final class BasicAuthentication(val userRepository: UserRepository) extends Authentication {

  protected def checkCredentials(
      email: String,
      password: String
  ): IO[Either[ErrorsResponse, UserInfo]] =
    userRepository.findByEmail(email) map {
      case Some(user) =>
        if (user.password == password) Right(UserInfo(id = user.id.get, enabled = user.enabled))
        else Left(ErrorsResponse(List(ErrorItem(errorDescription = "password is not correct"))))
      case None =>
        Left(
          ErrorsResponse(List(ErrorItem(errorDescription = s"user with email $email not found")))
        )
    }

}
